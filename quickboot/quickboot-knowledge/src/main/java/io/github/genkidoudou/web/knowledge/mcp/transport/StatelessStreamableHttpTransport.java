package io.github.genkidoudou.web.knowledge.mcp.transport;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.HttpHeaders;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpTransportException;
import io.modelcontextprotocol.spec.ProtocolVersions;
import io.modelcontextprotocol.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 无状态 Streamable HTTP 传输：仅通过 POST JSON-RPC 通信，不发起 GET /mcp SSE 长连接。
 * <p>
 * ModelScope 等托管 MCP 对 GET /mcp 返回 JSON 探活（{@code {"info":"return 200 for GET /mcp."}}），
 * 官方 {@link io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport}
 * 在初始化后会强制 GET 并以 SSE 解析，导致 {@code Invalid SSE response}。本实现用于兼容此类服务端。
 */
public class StatelessStreamableHttpTransport implements McpClientTransport {

    private static final Logger logger = LoggerFactory.getLogger(StatelessStreamableHttpTransport.class);

    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_EVENT_STREAM = "text/event-stream";

    private final McpJsonMapper jsonMapper;
    private final HttpClient httpClient;
    private final URI endpointUri;
    private final Map<String, String> extraHeaders;
    private final List<String> supportedProtocolVersions;
    private final String defaultProtocolVersion;

    private final AtomicReference<Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>>> handler =
        new AtomicReference<>();
    private final AtomicReference<String> sessionId = new AtomicReference<>();

    private StatelessStreamableHttpTransport(McpJsonMapper jsonMapper,
                                             HttpClient httpClient,
                                             URI endpointUri,
                                             Map<String, String> extraHeaders,
                                             List<String> supportedProtocolVersions) {
        this.jsonMapper = jsonMapper;
        this.httpClient = httpClient;
        this.endpointUri = endpointUri;
        this.extraHeaders = extraHeaders == null ? Map.of() : Map.copyOf(extraHeaders);
        this.supportedProtocolVersions = Collections.unmodifiableList(supportedProtocolVersions);
        this.defaultProtocolVersion = supportedProtocolVersions.get(supportedProtocolVersions.size() - 1);
    }

    public static Builder builder(String baseUri) {
        return new Builder(baseUri);
    }

    @Override
    public List<String> protocolVersions() {
        return supportedProtocolVersions;
    }

    @Override
    public Mono<Void> connect(
        Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>> messageHandler) {
        this.handler.set(messageHandler);
        return Mono.empty();
    }

    @Override
    public void setExceptionHandler(Consumer<Throwable> handler) {
        logger.debug("StatelessStreamableHttpTransport exception handler registered");
    }

    @Override
    public Mono<Void> closeGracefully() {
        sessionId.set(null);
        return Mono.empty();
    }

    @Override
    public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
        return Mono.deferContextual(ctx -> {
            String protocolVersion = ctx.getOrDefault(
                McpAsyncClient.NEGOTIATED_PROTOCOL_VERSION, defaultProtocolVersion);
            String body;
            try {
                body = jsonMapper.writeValueAsString(message);
            } catch (IOException ex) {
                return Mono.error(new RuntimeException("Failed to serialize JSON-RPC message", ex));
            }

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(endpointUri)
                .timeout(Duration.ofSeconds(120))
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON + ", " + TEXT_EVENT_STREAM)
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.PROTOCOL_VERSION, protocolVersion)
                .POST(HttpRequest.BodyPublishers.ofString(body));

            String currentSessionId = sessionId.get();
            if (currentSessionId != null) {
                requestBuilder.header(HttpHeaders.MCP_SESSION_ID, currentSessionId);
            }
            extraHeaders.forEach(requestBuilder::header);

            return Mono.fromFuture(httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString()))
                .flatMap(response -> handlePostResponse(message, response));
        });
    }

    private Mono<Void> handlePostResponse(McpSchema.JSONRPCMessage sentMessage, HttpResponse<String> response) {
        response.headers().firstValue(HttpHeaders.MCP_SESSION_ID).ifPresent(sessionId::set);

        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            return Mono.error(new McpTransportException(
                "POST " + endpointUri + " failed, status=" + statusCode + ", body=" + response.body()));
        }

        if (sentMessage instanceof McpSchema.JSONRPCNotification) {
            return Mono.empty();
        }

        String responseBody = response.body();
        if (responseBody == null || responseBody.isBlank()) {
            return Mono.empty();
        }

        try {
            McpSchema.JSONRPCMessage responseMessage = McpSchema.deserializeJsonRpcMessage(jsonMapper, responseBody);
            Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>> messageHandler = handler.get();
            if (messageHandler == null) {
                logger.warn("No message handler registered, dropping response for {}", sentMessage);
                return Mono.empty();
            }
            return messageHandler.apply(Mono.just(responseMessage)).then();
        } catch (IOException ex) {
            return Mono.error(new McpTransportException(
                "Error deserializing JSON-RPC response: " + responseBody, ex));
        }
    }

    @Override
    public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
        return jsonMapper.convertValue(data, typeRef);
    }

    /**
     * Builder。
     */
    public static final class Builder {

        private final String baseUri;
        private McpJsonMapper jsonMapper;
        private String endpoint = "/mcp";
        private HttpClient.Builder clientBuilder = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL);
        private Map<String, String> headers = Map.of();
        private List<String> supportedProtocolVersions = List.of(
            ProtocolVersions.MCP_2024_11_05,
            ProtocolVersions.MCP_2025_03_26,
            ProtocolVersions.MCP_2025_06_18
        );

        private Builder(String baseUri) {
            this.baseUri = baseUri;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder jsonMapper(McpJsonMapper jsonMapper) {
            this.jsonMapper = jsonMapper;
            return this;
        }

        public Builder customizeClient(Consumer<HttpClient.Builder> customizer) {
            customizer.accept(clientBuilder);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers == null ? Map.of() : headers;
            return this;
        }

        public Builder supportedProtocolVersions(List<String> versions) {
            this.supportedProtocolVersions = versions;
            return this;
        }

        public StatelessStreamableHttpTransport build() {
            if (jsonMapper == null) {
                throw new IllegalStateException("jsonMapper is required");
            }
            URI endpointUri = Utils.resolveUri(URI.create(baseUri), endpoint);
            return new StatelessStreamableHttpTransport(
                jsonMapper,
                clientBuilder.build(),
                endpointUri,
                headers,
                supportedProtocolVersions
            );
        }
    }
}
