package io.github.genkidoudou.web.workflow.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.workflow.config.WorkflowProperties;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流 HTTP 请求客户端：复用 knowledge 模块 SSRF 防护策略（内网拒绝、跳转复检、超时、大小限制）。
 */
@Component
public class WorkflowHttpClient {

    private final WorkflowProperties.HttpRequest config;

    public WorkflowHttpClient(WorkflowProperties properties) {
        this.config = properties.getHttpRequest();
    }

    /**
     * 执行 HTTP 请求。
     *
     * @param method  方法：GET/POST/PUT/DELETE
     * @param url     目标 URL 模板（已渲染）
     * @param headers 请求头
     * @param body    请求体，可为 null
     * @return status、headers、body
     */
    public Map<String, Object> execute(String method, String url, Map<String, String> headers, String body) {
        if (!config.isEnabled()) {
            throw new IllegalStateException("HTTP 请求节点未启用");
        }
        if (StrUtil.isBlank(url)) {
            throw new IllegalArgumentException("URL 不能为空");
        }
        String httpMethod = method == null ? "GET" : method.toUpperCase();
        URI currentUri = parseAndValidateUrl(url.trim());
        int redirects = 0;
        while (true) {
            validateHost(currentUri);
            HttpURLConnection connection;
            try {
                connection = openConnection(currentUri, httpMethod, headers, body);
            } catch (IOException ex) {
                throw new IllegalStateException("HTTP 请求失败：无法建立连接", ex);
            }
            try {
                int status = connection.getResponseCode();
                if (isRedirect(status)) {
                    String location = connection.getHeaderField("Location");
                    if (StrUtil.isBlank(location)) {
                        throw new IllegalStateException("HTTP 请求失败：重定向缺少 Location");
                    }
                    if (redirects >= config.getMaxRedirects()) {
                        throw new IllegalStateException("HTTP 请求失败：重定向次数超限");
                    }
                    currentUri = resolveRedirect(currentUri, location);
                    redirects++;
                    continue;
                }
                validateContentLength(connection.getHeaderFieldLong("Content-Length", -1L));
                String responseBody = readBody(connection, status);
                Map<String, Object> result = new HashMap<>();
                result.put("status", status);
                result.put("headers", extractHeaders(connection));
                result.put("body", responseBody);
                return result;
            } catch (IOException ex) {
                throw new IllegalStateException("HTTP 请求失败：" + ex.getMessage(), ex);
            } finally {
                connection.disconnect();
            }
        }
    }

    /**
     * 校验 URL 是否允许访问（SSRF 检测）。
     *
     * @param url 目标 URL
     */
    public void validateUrl(String url) {
        URI uri = parseAndValidateUrl(url);
        validateHost(uri);
    }

    private URI parseAndValidateUrl(String url) {
        URI uri;
        try {
            uri = URI.create(url).normalize();
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("HTTP 请求失败：URL 格式非法", ex);
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new IllegalStateException("HTTP 请求失败：仅允许 http/https 协议");
        }
        if (StrUtil.isBlank(uri.getHost())) {
            throw new IllegalStateException("HTTP 请求失败：URL 缺少主机名");
        }
        return uri;
    }

    private URI resolveRedirect(URI current, String location) {
        URI next;
        try {
            next = URI.create(location).normalize();
            if (next.getHost() == null) {
                next = current.resolve(location).normalize();
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("HTTP 请求失败：重定向 URL 非法", ex);
        }
        String scheme = next.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new IllegalStateException("HTTP 请求失败：重定向目标协议非法");
        }
        return next;
    }

    private void validateHost(URI uri) {
        String host = uri.getHost().toLowerCase();
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (isBlockedAddress(address)) {
                    throw new IllegalStateException("HTTP 请求失败：禁止访问内网或本地地址");
                }
            }
        } catch (UnknownHostException ex) {
            throw new IllegalStateException("HTTP 请求失败：无法解析主机 " + host, ex);
        }
    }

    static boolean isBlockedAddress(InetAddress address) {
        if (address.isAnyLocalAddress()
            || address.isLoopbackAddress()
            || address.isLinkLocalAddress()
            || address.isSiteLocalAddress()) {
            return true;
        }
        byte[] bytes = address.getAddress();
        if (bytes.length == 4) {
            int b0 = bytes[0] & 0xFF;
            int b1 = bytes[1] & 0xFF;
            return b0 == 169 && b1 == 254;
        }
        return false;
    }

    private HttpURLConnection openConnection(URI uri, String method, Map<String, String> headers, String body)
        throws IOException {
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(config.getTimeoutMs());
        connection.setReadTimeout(config.getTimeoutMs());
        connection.setRequestMethod(method);
        connection.setRequestProperty("User-Agent", config.getUserAgent());
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (StrUtil.isNotBlank(entry.getKey()) && entry.getValue() != null) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
        }
        if (body != null && !body.isBlank() && !"GET".equals(method) && !"HEAD".equals(method)) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        return connection;
    }

    private boolean isRedirect(int status) {
        return status == HttpURLConnection.HTTP_MOVED_PERM
            || status == HttpURLConnection.HTTP_MOVED_TEMP
            || status == HttpURLConnection.HTTP_SEE_OTHER
            || status == 307
            || status == 308;
    }

    private void validateContentLength(long contentLength) {
        if (contentLength > 0 && contentLength > config.getMaxBytes()) {
            throw new IllegalStateException("HTTP 请求失败：响应体超过大小限制");
        }
    }

    private String readBody(HttpURLConnection connection, int status) throws IOException {
        InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (input == null) {
            return "";
        }
        try (InputStream in = input) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8_192];
            int total = 0;
            int read;
            while ((read = in.read(chunk)) != -1) {
                total += read;
                if (total > config.getMaxBytes()) {
                    throw new IllegalStateException("HTTP 请求失败：响应体超过大小限制");
                }
                buffer.write(chunk, 0, read);
            }
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> extractHeaders(HttpURLConnection connection) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                headers.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return headers;
    }
}
