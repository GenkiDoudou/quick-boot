package io.github.genkidoudou.web.knowledge.mcp.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * 远程 MCP URL 的 SSRF 校验，策略对齐 {@link io.github.genkidoudou.web.knowledge.ingest.web.WebContentFetcher#validateUrl}。
 */
@Component
public class McpUrlGuard {

    /**
     * 校验远程 MCP URL 是否允许访问。
     *
     * @param url 目标 URL，仅允许 http/https
     * @throws WarningException SSRF 或协议非法
     */
    public void validateUrl(String url) {
        if (StrUtil.isBlank(url)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "远程 MCP URL 不能为空");
        }
        URI uri = parseAndValidateUrl(url.trim());
        validateHost(uri);
    }

    private URI parseAndValidateUrl(String url) {
        URI uri;
        try {
            uri = URI.create(url).normalize();
        } catch (IllegalArgumentException ex) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "远程 MCP URL 格式非法");
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "远程 MCP URL 仅允许 http/https 协议");
        }
        if (StrUtil.isBlank(uri.getHost())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "远程 MCP URL 缺少主机名");
        }
        return uri;
    }

    private void validateHost(URI uri) {
        String host = uri.getHost().toLowerCase();
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (isBlockedAddress(address)) {
                    throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "远程 MCP URL 禁止访问内网或本地地址");
                }
            }
        } catch (UnknownHostException ex) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "远程 MCP URL 无法解析主机: " + host);
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
}
