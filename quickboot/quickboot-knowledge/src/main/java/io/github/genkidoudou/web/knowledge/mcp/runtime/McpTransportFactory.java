package io.github.genkidoudou.web.knowledge.mcp.runtime;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.config.KnowledgeMcpProperties;
import io.github.genkidoudou.web.knowledge.constants.McpTransport;
import io.github.genkidoudou.web.knowledge.mcp.support.McpUrlGuard;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import org.springframework.stereotype.Component;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

/**
 * 按解析后的配置构建 MCP Java SDK {@link io.modelcontextprotocol.spec.McpClientTransport}。
 * <p>
 * 注：MCP SDK 0.10.0 核心包仅提供 STDIO 与 SSE Transport；{@code STREAMABLE_HTTP} 暂以 SSE Transport 连接（endpoint {@code /mcp}）。
 */
@Component
public class McpTransportFactory {

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
            return createHttpTransport(config, null);
        }
        if (McpTransport.STREAMABLE_HTTP.equals(transport)) {
            return createHttpTransport(config, "/mcp");
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
        return new StdioClientTransport(builder.build());
    }

    private io.modelcontextprotocol.spec.McpClientTransport createHttpTransport(McpResolvedConfig config, String sseEndpoint) {
        urlGuard.validateUrl(config.getUrl());
        HttpClientSseClientTransport.Builder builder = HttpClientSseClientTransport.builder(config.getUrl().trim());
        if (StrUtil.isNotBlank(sseEndpoint)) {
            builder.sseEndpoint(sseEndpoint);
        }
        applyHttpHeaders(builder, config);
        return builder.build();
    }

    private void applyHttpHeaders(HttpClientSseClientTransport.Builder builder, McpResolvedConfig config) {
        Map<String, String> headers = config.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return;
        }
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        headers.forEach(requestBuilder::header);
        builder.requestBuilder(requestBuilder);
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
