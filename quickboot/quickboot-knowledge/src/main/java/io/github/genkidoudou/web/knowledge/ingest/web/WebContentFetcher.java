package io.github.genkidoudou.web.knowledge.ingest.web;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网页正文抓取：HTTP GET + SSRF 防护 + 超时与响应体大小限制。
 */
@Component
public class WebContentFetcher {

    private final KnowledgeProperties.WebFetch config;

    public WebContentFetcher(KnowledgeProperties properties) {
        this.config = properties.getWebFetch();
    }

    /**
     * 抓取 URL 正文（HTML 或纯文本）。
     *
     * @param url 目标地址，仅允许 http/https
     * @return 响应体文本
     */
    public String fetch(String url) {
        if (!config.isEnabled()) {
            throw new IllegalStateException("网页抓取功能未启用");
        }
        if (StrUtil.isBlank(url)) {
            throw new IllegalArgumentException("URL 不能为空");
        }
        URI currentUri = parseAndValidateUrl(url.trim());
        int redirects = 0;
        while (true) {
            validateHost(currentUri);
            HttpURLConnection connection = openConnection(currentUri);
            try {
                int status = connection.getResponseCode();
                if (isRedirect(status)) {
                    String location = connection.getHeaderField("Location");
                    if (StrUtil.isBlank(location)) {
                        throw new IllegalStateException("网页抓取失败：重定向缺少 Location");
                    }
                    if (redirects >= config.getMaxRedirects()) {
                        throw new IllegalStateException("网页抓取失败：重定向次数超限");
                    }
                    currentUri = resolveRedirect(currentUri, location);
                    redirects++;
                    continue;
                }
                if (status < 200 || status >= 300) {
                    throw new IllegalStateException("网页抓取失败：HTTP " + status);
                }
                validateContentLength(connection.getHeaderFieldLong("Content-Length", -1L));
                return readBody(connection);
            } catch (IOException ex) {
                throw new IllegalStateException("网页抓取失败：" + ex.getMessage(), ex);
            } finally {
                connection.disconnect();
            }
        }
    }

    /**
     * 校验 URL 是否允许访问（供单测与内部复用）。
     *
     * @param url 目标 URL
     * @throws IllegalStateException SSRF 或协议非法
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
            throw new IllegalStateException("网页抓取失败：URL 格式非法", ex);
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new IllegalStateException("网页抓取失败：仅允许 http/https 协议");
        }
        if (StrUtil.isBlank(uri.getHost())) {
            throw new IllegalStateException("网页抓取失败：URL 缺少主机名");
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
            throw new IllegalStateException("网页抓取失败：重定向 URL 非法", ex);
        }
        String scheme = next.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new IllegalStateException("网页抓取失败：重定向目标协议非法");
        }
        return next;
    }

    private void validateHost(URI uri) {
        String host = uri.getHost().toLowerCase();
        List<String> allowedHosts = config.getAllowedHosts();
        if (allowedHosts != null && !allowedHosts.isEmpty()) {
            boolean allowed = allowedHosts.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .map(String::toLowerCase)
                .anyMatch(host::equals);
            if (!allowed) {
                throw new IllegalStateException("网页抓取失败：主机不在白名单内");
            }
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (isBlockedAddress(address)) {
                    throw new IllegalStateException("网页抓取失败：禁止访问内网或本地地址");
                }
            }
        } catch (UnknownHostException ex) {
            throw new IllegalStateException("网页抓取失败：无法解析主机 " + host, ex);
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
            if (b0 == 169 && b1 == 254) {
                return true;
            }
        }
        return false;
    }

    private HttpURLConnection openConnection(URI uri) {
        try {
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(config.getTimeoutMs());
            connection.setReadTimeout(config.getTimeoutMs());
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", config.getUserAgent());
            connection.setRequestProperty("Accept", "text/html,text/plain,*/*");
            return connection;
        } catch (IOException ex) {
            throw new IllegalStateException("网页抓取失败：无法建立连接", ex);
        }
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
            throw new IllegalStateException("网页抓取失败：响应体超过大小限制");
        }
    }

    private String readBody(HttpURLConnection connection) throws IOException {
        try (InputStream input = connection.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8_192];
            int total = 0;
            int read;
            while ((read = input.read(chunk)) != -1) {
                total += read;
                if (total > config.getMaxBytes()) {
                    throw new IllegalStateException("网页抓取失败：响应体超过大小限制");
                }
                buffer.write(chunk, 0, read);
            }
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
