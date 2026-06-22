package io.github.genkidoudou.web.knowledge.mcp.runtime;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.config.KnowledgeMcpProperties;
import io.github.genkidoudou.web.knowledge.constants.McpTransport;
import io.github.genkidoudou.web.knowledge.mcp.support.McpTransportUrlSupport;
import io.github.genkidoudou.web.knowledge.mcp.support.McpTransportUrlSupport.StreamableHttpUrlParts;
import io.github.genkidoudou.web.knowledge.mcp.support.McpUrlGuard;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

/**
 * 按解析后的配置构建 MCP Java SDK {@link io.modelcontextprotocol.spec.McpClientTransport}。
 */
@Component
public class McpTransportFactory {

    /** MCP SDK 0.18+ 各 Transport 需显式传入 JSON 映射器。 */
    private static final McpJsonMapper JSON_MAPPER = new JacksonMcpJsonMapper(new ObjectMapper());

    private final KnowledgeMcpProperties properties;
    private final McpUrlGuard urlGuard;

    public McpTransportFactory(KnowledgeMcpProperties properties, McpUrlGuard urlGuard) {
        this.properties = properties;
        this.urlGuard = urlGuard;
    }

    /**
     * 根据传输方式创建 MCP Transport。
     *
     * @param config 已解析的运行时配置
     * @return MCP Transport 实例
     */
    public io.modelcontextprotocol.spec.McpClientTransport createTransport(McpResolvedConfig config) {
        String transport = config.getTransport();
        if (McpTransport.STDIO.equals(transport)) {
            return createStdioTransport(config);
        }
        if (McpTransport.SSE.equals(transport)) {
            return createSseTransport(config);
        }
        if (McpTransport.STREAMABLE_HTTP.equals(transport)) {
            return createStreamableHttpTransport(config);
        }
        throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不支持的传输方式: " + transport);
    }

    private io.modelcontextprotocol.spec.McpClientTransport createStdioTransport(McpResolvedConfig config) {
        if (StrUtil.isBlank(config.getCommand())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "STDIO 命令不能为空");
        }
        validateStdioCommand(config.getCommand());
        ServerParameters.Builder builder = ServerParameters.builder(config.getCommand().trim());
        List<String> args = config.getArgs();
        if (args != null && !args.isEmpty()) {
            builder.args(args);
        }
        Map<String, String> env = config.getEnv();
        if (env != null && !env.isEmpty()) {
            builder.env(env);
        }
        return new StdioClientTransport(builder.build(), JSON_MAPPER);
    }

    /**
     * 遗留 HTTP+SSE 传输（服务端提供 /sse 等 SSE 端点）。
     */
    private io.modelcontextprotocol.spec.McpClientTransport createSseTransport(McpResolvedConfig config) {
        urlGuard.validateUrl(config.getUrl());
        HttpClientSseClientTransport.Builder builder = HttpClientSseClientTransport.builder(config.getUrl().trim())
            .jsonMapper(JSON_MAPPER);
        applySseHeaders(builder, config);
        return builder.build();
    }

    /**
     * Streamable HTTP（2025-03-26 协议）：用户配置为完整 MCP 端点 URL，须拆为 baseUri + endpoint 再交给 SDK。
     */
    private io.modelcontextprotocol.spec.McpClientTransport createStreamableHttpTransport(McpResolvedConfig config) {
        urlGuard.validateUrl(config.getUrl());
        StreamableHttpUrlParts parts = McpTransportUrlSupport.splitStreamableHttpUrl(config.getUrl());
        HttpClientStreamableHttpTransport.Builder builder =
            HttpClientStreamableHttpTransport.builder(parts.baseUri())
                .endpoint(parts.endpoint())
                .jsonMapper(JSON_MAPPER)
                // ModelScope 等托管 MCP 使用 HTTP/1.1；跟随重定向以兼容 /mcp 与 /mcp/ 差异
                .customizeClient(client -> client
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL));
        applyStreamableHttpHeaders(builder, config);
        return builder.build();
    }

    private void applySseHeaders(HttpClientSseClientTransport.Builder builder, McpResolvedConfig config) {
        Map<String, String> headers = config.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return;
        }
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        headers.forEach(requestBuilder::header);
        builder.requestBuilder(requestBuilder);
    }

    private void applyStreamableHttpHeaders(HttpClientStreamableHttpTransport.Builder builder, McpResolvedConfig config) {
        Map<String, String> headers = config.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return;
        }
        builder.customizeRequest(requestBuilder -> headers.forEach(requestBuilder::setHeader));
    }

    private void validateStdioCommand(String command) {
        List<String> allowed = properties.getStdio().getAllowedCommands();
        if (allowed == null || allowed.isEmpty()) {
            return;
        }
        String first = command.trim();
        int space = first.indexOf(' ');
        if (space > 0) {
            first = first.substring(0, space);
        }
        String normalized = first.toLowerCase();
        boolean ok = allowed.stream()
            .filter(StrUtil::isNotBlank)
            .map(String::trim)
            .map(String::toLowerCase)
            .anyMatch(normalized::equals);
        if (!ok) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "STDIO 命令不在白名单内: " + command);
        }
    }
}
