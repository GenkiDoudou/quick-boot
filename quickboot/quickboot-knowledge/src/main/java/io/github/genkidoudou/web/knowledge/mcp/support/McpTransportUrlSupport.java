package io.github.genkidoudou.web.knowledge.mcp.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.constants.McpTransport;

import java.net.URI;
import java.util.Locale;

/**
 * 根据 URL 形态推断远程 MCP 传输方式，避免将 Streamable HTTP 端点误配为 SSE。
 */
public final class McpTransportUrlSupport {

    private static final String DEFAULT_STREAMABLE_HTTP_ENDPOINT = "/mcp";

    private McpTransportUrlSupport() {
    }

    /**
     * Streamable HTTP 连接所需的 baseUri 与 endpoint。
     *
     * @param baseUri  仅含 scheme 与 authority，如 {@code https://mcp.api-inference.modelscope.net}
     * @param endpoint 以 {@code /} 开头的路径（可含 query），如 {@code /4da54faa775e47/mcp}
     */
    public record StreamableHttpUrlParts(String baseUri, String endpoint) {
    }

    /**
     * 将用户配置的完整 MCP URL 拆为 SDK {@link io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport}
     * 所需的 baseUri + endpoint。
     * <p>
     * SDK 的 {@code builder(baseUri)} 默认 endpoint 为 {@code /mcp}；若把完整 URL 直接传入 baseUri，
     * {@link URI#resolve(String)} 会用 {@code /mcp} 覆盖已有路径，导致 ModelScope 等带部署 ID 的地址 404。
     */
    public static StreamableHttpUrlParts splitStreamableHttpUrl(String url) {
        if (StrUtil.isBlank(url)) {
            throw new IllegalArgumentException("Streamable HTTP URL 不能为空");
        }
        URI uri = URI.create(url.trim());
        if (uri.getScheme() == null || uri.getAuthority() == null) {
            throw new IllegalArgumentException("Streamable HTTP URL 须为绝对地址: " + url);
        }
        String baseUri = uri.getScheme() + "://" + uri.getAuthority();
        String path = uri.getRawPath();
        if (StrUtil.isBlank(path) || "/".equals(path)) {
            path = DEFAULT_STREAMABLE_HTTP_ENDPOINT;
        }
        if (uri.getRawQuery() != null) {
            path = path + "?" + uri.getRawQuery();
        }
        if (uri.getRawFragment() != null) {
            path = path + "#" + uri.getRawFragment();
        }
        return new StreamableHttpUrlParts(baseUri, path);
    }

    /**
     * URL 是否像 Streamable HTTP 端点（如 ModelScope {@code .../mcp}）。
     */
    public static boolean looksLikeStreamableHttpUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return false;
        }
        String trimmed = url.trim();
        String path = extractPath(trimmed);
        if (path.endsWith("/mcp") || path.endsWith("/mcp/")) {
            return true;
        }
        String host = extractHost(trimmed);
        return host != null && host.contains("mcp.api-inference.modelscope.net");
    }

    /**
     * 传输方式与 URL 是否明显不匹配。
     */
    public static boolean isTransportMismatch(String transport, String url) {
        if (!McpTransport.SSE.equals(transport)) {
            return false;
        }
        return looksLikeStreamableHttpUrl(url);
    }

    /**
     * 保存/测试时的友好提示。
     */
    public static String transportMismatchHint(String transport, String url) {
        if (!isTransportMismatch(transport, url)) {
            return null;
        }
        return "当前 URL 形如 Streamable HTTP 端点（如 ModelScope .../mcp），请将传输方式改为「Streamable HTTP」，"
            + "不要使用 SSE；SSE 适用于提供 /sse 事件流的旧版 MCP 服务。";
    }

    /**
     * 连接失败时根据异常与配置补充说明。
     */
    public static String enrichFailureMessage(String transport, String url, String rawMessage) {
        String base = StrUtil.blankToDefault(rawMessage, "未知错误");
        String hint = transportMismatchHint(transport, url);
        if (hint != null && (base.contains("SSE") || base.contains("404") || base.contains("record not found"))) {
            return base + "。提示：" + hint;
        }
        if (base.contains("record not found")) {
            return base + "。提示：远程 MCP 实例可能已过期或被删除，请在 ModelScope 控制台重新获取 URL。";
        }
        return base;
    }

    private static String extractPath(String url) {
        try {
            String path = URI.create(url).getPath();
            return path == null ? "" : path.toLowerCase(Locale.ROOT);
        } catch (Exception ex) {
            return "";
        }
    }

    private static String extractHost(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception ex) {
            return null;
        }
    }
}
