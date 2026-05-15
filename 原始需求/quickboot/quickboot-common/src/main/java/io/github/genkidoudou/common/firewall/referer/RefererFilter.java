package io.github.genkidoudou.common.firewall.referer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求来源拦截过滤器
 * 
 * 拦截不允许的 Referer 来源
 *
 * @author QuickBoot
 * @since 2026/03/03
 */
public class RefererFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RefererFilter.class);

    private final RefererProperties properties;
    private final ObjectMapper objectMapper;
    private final PathMatcher pathMatcher;

    public RefererFilter(RefererProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("请求来源拦截过滤器已初始化");
        log.info("允许的 Referer: {}", properties.getAllowedReferers());
        log.info("允许空 Referer: {}", properties.getAllowEmptyReferer());
        log.info("排除的 URL: {}", properties.getExcludeUrls());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // 类型转换
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 检查是否在排除列表中
        if (isExcluded(httpRequest)) {
            log.debug("请求 {} 在排除列表中，跳过拦截", httpRequest.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        // 验证 Referer
        if (!isRefererAllowed(httpRequest)) {
            String referer = httpRequest.getHeader("Referer");
            String uri = httpRequest.getRequestURI();
            log.warn("请求被拦截: 不允许的 Referer - referer={}, uri={}", referer, uri);
            throw RefererException.notAllowed(referer);
        }

        // 放行请求
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        log.info("请求来源拦截过滤器已销毁");
    }

    /**
     * 检查请求是否在排除列表中
     */
    private boolean isExcluded(HttpServletRequest request) {
        String uri = request.getRequestURI();
        List<String> excludeUrls = properties.getExcludeUrls();
        
        if (excludeUrls == null || excludeUrls.isEmpty()) {
            return false;
        }

        for (String pattern : excludeUrls) {
            if (pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查 Referer 是否允许
     */
    private boolean isRefererAllowed(HttpServletRequest request) {
        List<String> allowedReferers = properties.getAllowedReferers();
        
        // 如果未配置，默认允许所有
        if (allowedReferers == null || allowedReferers.isEmpty()) {
            return true;
        }

        // 获取 Referer 头
        String referer = request.getHeader("Referer");
        
        // 处理空 Referer
        if (!StringUtils.hasText(referer)) {
            boolean allowed = properties.getAllowEmptyReferer();
            if (!allowed) {
                log.debug("拒绝空 Referer");
            }
            return allowed;
        }

        // 检查是否匹配任何允许的 Referer
        for (String allowedReferer : allowedReferers) {
            if (matchReferer(referer, allowedReferer)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 匹配 Referer
     * 支持通配符匹配
     */
    private boolean matchReferer(String referer, String pattern) {
        try {
            // 标准化 Referer（移除末尾的斜杠）
            String normalizedReferer = normalizeUrl(referer);
            String normalizedPattern = normalizeUrl(pattern);

            // 精确匹配
            if (normalizedReferer.equals(normalizedPattern)) {
                return true;
            }

            // 前缀匹配（pattern 以 / 结尾）
            if (pattern.endsWith("/") && normalizedReferer.startsWith(normalizedPattern)) {
                return true;
            }

            // 通配符匹配：https://*.example.com/
            if (pattern.contains("*")) {
                return matchWildcard(normalizedReferer, normalizedPattern);
            }

            return false;
        } catch (Exception e) {
            log.debug("Referer 匹配失败: referer={}, pattern={}", referer, pattern, e);
            return false;
        }
    }

    /**
     * 通配符匹配
     */
    private boolean matchWildcard(String referer, String pattern) {
        try {
            URI refererUri = new URI(referer);
            URI patternUri = new URI(pattern);

            // 协议必须匹配
            if (!refererUri.getScheme().equals(patternUri.getScheme())) {
                return false;
            }

            // 端口必须匹配
            if (refererUri.getPort() != patternUri.getPort()) {
                return false;
            }

            // 主机名通配符匹配
            String refererHost = refererUri.getHost();
            String patternHost = patternUri.getHost();

            if (patternHost.startsWith("*.")) {
                // *.example.com 匹配 sub.example.com 和 example.com
                String domain = patternHost.substring(2);
                return refererHost.equals(domain) || refererHost.endsWith("." + domain);
            }

            return refererHost.equals(patternHost);
        } catch (Exception e) {
            log.debug("通配符匹配失败", e);
            return false;
        }
    }

    /**
     * 标准化 URL
     */
    private String normalizeUrl(String url) {
        if (url == null) {
            return "";
        }
        // 移除末尾的斜杠（除非是根路径）
        if (url.endsWith("/") && url.length() > 1) {
            try {
                URI uri = new URI(url);
                if (uri.getPath().equals("/")) {
                    return url;
                }
                return url.substring(0, url.length() - 1);
            } catch (Exception e) {
                return url;
            }
        }
        return url;
    }

    /**
     * 发送 403 响应
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 403);
        result.put("message", message);
        result.put("timestamp", LocalDateTime.now().toString());

        String json = objectMapper.writeValueAsString(result);
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
