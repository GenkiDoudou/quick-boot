package io.github.genkidoudou.web.system.oauthclient.clientsign;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 解析用于 Client 签名与接口授权的 servlet path，并与前端 {@code clientSign.js} 对齐。
 * <p>
 * 若 Nginx 未剥离 {@code /prod-api}、{@code /dev-api} 前缀，在此统一去掉，避免签名校验 path 不一致。
 */
public final class ClientRequestPathSupport {

    private static final String[] GATEWAY_PREFIXES = {"/prod-api", "/dev-api"};

    private ClientRequestPathSupport() {
    }

    /**
     * @param request 当前请求
     * @return 用于签名与 Ant 路径匹配的 path（以 {@code /} 开头，不含 query）
     */
    public static String resolveServletPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String path;
        if (StrUtil.isNotBlank(servletPath)) {
            path = servletPath;
        } else {
            String uri = request.getRequestURI();
            String ctx = request.getContextPath();
            if (StrUtil.isNotBlank(ctx) && uri.startsWith(ctx)) {
                path = uri.substring(ctx.length());
            } else {
                path = uri;
            }
        }
        int q = path.indexOf('?');
        if (q >= 0) {
            path = path.substring(0, q);
        }
        return stripGatewayPrefix(path);
    }

    /**
     * 去掉网关/API 前缀，与前端 resolveSignPath 行为一致。
     *
     * @param path 原始 path
     * @return 规范化后的 path
     */
    public static String stripGatewayPrefix(String path) {
        if (StrUtil.isBlank(path)) {
            return "/";
        }
        String p = path.trim();
        for (String prefix : GATEWAY_PREFIXES) {
            if (p.equals(prefix)) {
                return "/";
            }
            if (p.startsWith(prefix + "/")) {
                p = p.substring(prefix.length());
                break;
            }
        }
        return p.startsWith("/") ? p : "/" + p;
    }
}
