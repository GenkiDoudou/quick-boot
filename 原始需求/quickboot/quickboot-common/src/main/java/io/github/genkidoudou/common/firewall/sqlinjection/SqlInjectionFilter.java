package io.github.genkidoudou.common.firewall.sqlinjection;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.core.GlobalMsgCode;
import io.github.genkidoudou.common.core.R;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL注入检测过滤器
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
@RequiredArgsConstructor
public class SqlInjectionFilter extends OncePerRequestFilter {

    private final SqlInjectionProperties properties;
    private final SqlKeywordsProvider sqlKeywordsProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 检查是否忽略
        if (ignore(request, properties.getIgnoreUrls())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 检查 GET 参数
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            for (String val : entry.getValue()) {
                List<String> hits = SqlInjectionUtils.detect(val, sqlKeywordsProvider.getKeywords());
                if (!hits.isEmpty()) {
                    handleBlock(response, request, entry.getKey(), hits);
                    return;
                }
            }
        }

        // 检查 JSON body
        if (isJsonRequest(request)) {
            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            if (!body.isEmpty()) {
                List<String> hits = detectInJson(body);
                if (!hits.isEmpty()) {
                    handleBlock(response, request, "body", hits);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 检查是否忽略
     *
     * @param request 请求
     * @param ignores 忽略列表
     * @return 是否忽略
     * @since 2026/03/05
     */
    private boolean ignore(HttpServletRequest request, List<String> ignores) {
        String path = request.getRequestURI();
        return ignores != null && ignores.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    /**
     * 检测JSON中的SQL注入
     *
     * @param json JSON字符串
     * @return 检测到的关键字列表
     * @since 2026/03/05
     */
    private List<String> detectInJson(String json) {
        List<String> hits = new ArrayList<>();
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            traverse(obj, hits);
        } catch (Exception e) {
            // 如果不是JSON，直接检测字符串
            hits.addAll(SqlInjectionUtils.detect(json, sqlKeywordsProvider.getKeywords()));
        }
        return hits;
    }

    /**
     * 遍历对象检测SQL注入
     *
     * @param obj  对象
     * @param hits 检测到的关键字列表
     * @since 2026/03/05
     */
    private void traverse(Object obj, List<String> hits) {
        if (obj == null) {
            return;
        }
        if (obj instanceof CharSequence) {
            hits.addAll(SqlInjectionUtils.detect(obj.toString(), sqlKeywordsProvider.getKeywords()));
            return;
        }
        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            map.values().forEach(v -> traverse(v, hits));
        } else if (obj instanceof Iterable<?>) {
            Iterable<?> it = (Iterable<?>) obj;
            it.forEach(v -> traverse(v, hits));
        }
    }

    /**
     * 判断是否为JSON请求
     *
     * @param request 请求
     * @return 是否为JSON请求
     * @since 2026/03/05
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * 处理拦截
     *
     * @param response 响应
     * @param request  请求
     * @param param    参数名
     * @param hits     检测到的关键字
     * @throws IOException IO异常
     * @since 2026/03/05
     */
    private void handleBlock(HttpServletResponse response, HttpServletRequest request,
                             String param, List<String> hits) throws IOException {
        log.warn("SQL注入拦截: url={}, ip={}, param={}, keywords={}",
                request.getRequestURI(), getClientIP(request), param, hits);

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        R<Void> result = R.error(GlobalMsgCode.BAD_REQUEST, "请求参数包含非法字符");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * 获取客户端IP
     *
     * @param request 请求
     * @return 客户端IP
     * @since 2026/03/05
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
