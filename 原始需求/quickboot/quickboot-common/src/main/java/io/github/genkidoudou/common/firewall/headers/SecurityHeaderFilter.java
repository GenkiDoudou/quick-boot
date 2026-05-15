package io.github.genkidoudou.common.firewall.headers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 安全头过滤器
 * <p>
 * 为 HTTP 响应添加安全相关头，防止点击劫持、XSS、MIME 嗅探等攻击
 *
 * @author genkidoudou
 * @since 2026/03/06
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityHeaderFilter extends OncePerRequestFilter {

    private final SecurityHeaderProperties properties;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 检查是否启用
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 检查是否在排除列表中
        if (isExcluded(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 添加安全头（在响应提交前设置）
        addSecurityHeaders(response);

        filterChain.doFilter(request, response);
    }

    /**
     * 添加安全头到响应
     *
     * @param response HTTP 响应
     * @since 2026/03/06
     */
    private void addSecurityHeaders(HttpServletResponse response) {
        // X-Frame-Options：防止点击劫持
        addHeaderIfPresent(response, "X-Frame-Options", properties.getFrameOptions());

        // X-Content-Type-Options：防止 MIME 嗅探
        addHeaderIfPresent(response, "X-Content-Type-Options", properties.getContentTypeOptions());

        // X-XSS-Protection：启用 XSS 过滤器
        addHeaderIfPresent(response, "X-XSS-Protection", properties.getXssProtection());

        // Content-Security-Policy：内容安全策略
        addHeaderIfPresent(response, "Content-Security-Policy", properties.getContentSecurityPolicy());

        // Strict-Transport-Security：强制 HTTPS
        addHeaderIfPresent(response, "Strict-Transport-Security", properties.getStrictTransportSecurity());

        // Referrer-Policy：Referer 控制
        addHeaderIfPresent(response, "Referrer-Policy", properties.getReferrerPolicy());

        // Permissions-Policy：功能权限控制
        addHeaderIfPresent(response, "Permissions-Policy", properties.getPermissionsPolicy());
    }

    /**
     * 当值非空时添加响应头
     *
     * @param response HTTP 响应
     * @param name     头名称
     * @param value    头值
     * @since 2026/03/06
     */
    private void addHeaderIfPresent(HttpServletResponse response, String name, String value) {
        if (StringUtils.hasText(value)) {
            response.setHeader(name, value);
        }
    }

    /**
     * 检查请求是否在排除列表中
     *
     * @param request HTTP 请求
     * @return 是否排除
     * @since 2026/03/06
     */
    private boolean isExcluded(HttpServletRequest request) {
        List<String> excludeUrls = properties.getExcludeUrls();
        if (excludeUrls == null || excludeUrls.isEmpty()) {
            return false;
        }

        String uri = request.getRequestURI();
        for (String pattern : excludeUrls) {
            if (pathMatcher.match(pattern, uri)) {
                log.debug("请求 {} 在安全头排除列表中，跳过", uri);
                return true;
            }
        }
        return false;
    }
}
