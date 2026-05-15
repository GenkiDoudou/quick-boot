package io.github.genkidoudou.common.firewall.methodandhost;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.core.R;
import io.github.genkidoudou.common.exception.ErrorCode;
import io.github.genkidoudou.common.utils.ServletUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求方式和域名拦截过滤器
 * <p>
 * 拦截不允许的 HTTP 请求方式和访问域名
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
public class MethodAndHostFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MethodAndHostFilter.class);

    private final MethodAndHostProperties properties;
    private final ObjectMapper objectMapper;
    private final PathMatcher pathMatcher;

    public MethodAndHostFilter(MethodAndHostProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.pathMatcher = new AntPathMatcher();
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 检查是否在排除列表中
        if (isExcluded(request)) {
            log.debug("请求 {} 在排除列表中，跳过拦截", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // 验证请求方式
        if (!isMethodAllowed(request)) {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            log.warn("请求被拦截: 不允许的请求方式 - method={}, uri={}", method, uri);
            ServletUtils.writeResponse(response, ErrorCode.METHOD_NOT_ALLOWED);
            return;
        }

        // 验证请求域名
        if (!isHostAllowed(request)) {
            String host = request.getHeader("Host");
            String uri = request.getRequestURI();
            log.warn("请求被拦截: 不允许的域名 - host={}, uri={}", host, uri);
            ServletUtils.writeResponse(response, ErrorCode.METHOD_NOT_ALLOWED);
            return;
        }

        // 放行请求
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        log.info("请求方式和域名拦截过滤器已销毁");
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
     * 检查请求方式是否允许
     */
    private boolean isMethodAllowed(HttpServletRequest request) {
        List<String> allowedMethods = properties.getAllowedMethods();

        // 如果未配置，默认允许所有方式
        if (allowedMethods == null || allowedMethods.isEmpty()) {
            return true;
        }

        String method = request.getMethod();
        return allowedMethods.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(method));
    }

    /**
     * 检查请求域名是否允许
     */
    private boolean isHostAllowed(HttpServletRequest request) {
        List<String> allowedHosts = properties.getAllowedHosts();

        // 如果未配置，默认允许所有域名
        if (allowedHosts == null || allowedHosts.isEmpty()) {
            return true;
        }

        // 获取 Host 头
        String host = getHost(request);
        if (host == null || host.isEmpty()) {
            log.warn("请求缺少 Host 头");
            return false;
        }

        // 检查是否匹配任何允许的域名
        for (String allowedHost : allowedHosts) {
            if (matchHost(host, allowedHost)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取请求的 Host
     */
    private String getHost(HttpServletRequest request) {
        // 优先从 X-Forwarded-Host 获取（代理场景）
        String host = request.getHeader("X-Forwarded-Host");
        if (host != null && !host.isEmpty()) {
            return host.split(",")[0].trim();
        }

        // 从 Host 头获取
        host = request.getHeader("Host");
        if (host != null && !host.isEmpty()) {
            return host;
        }

        // 从 ServerName 和 ServerPort 构造
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        // 默认端口不显示
        if ((serverPort == 80 && "http".equals(request.getScheme())) ||
                (serverPort == 443 && "https".equals(request.getScheme()))) {
            return serverName;
        }

        return serverName + ":" + serverPort;
    }

    /**
     * 匹配 Host
     * 支持通配符匹配
     */
    private boolean matchHost(String host, String pattern) {
        // 精确匹配
        if (host.equals(pattern)) {
            return true;
        }

        // 通配符匹配：*.example.com
        if (pattern.startsWith("*.")) {
            String domain = pattern.substring(2);
            return host.endsWith("." + domain) || host.equals(domain);
        }

        // 端口通配符：localhost:*
        if (pattern.endsWith(":*")) {
            String hostWithoutPort = host.split(":")[0];
            String patternWithoutPort = pattern.substring(0, pattern.length() - 2);
            return hostWithoutPort.equals(patternWithoutPort);
        }

        return false;
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
