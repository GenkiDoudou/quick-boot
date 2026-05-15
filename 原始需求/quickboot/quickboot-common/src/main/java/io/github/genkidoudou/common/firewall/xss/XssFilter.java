package io.github.genkidoudou.common.firewall.xss;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.core.GlobalMsgCode;
import io.github.genkidoudou.common.core.R;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * XSS 脚本注入检测过滤器
 * <p>
 * 拦截请求参数和 body，检测并阻止包含 XSS 脚本的请求
 *
 * @author genkidoudou
 * @since 2026/03/06
 */
@Slf4j
@RequiredArgsConstructor
public class XssFilter extends OncePerRequestFilter {

    private final XssProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 检查是否忽略
        if (ignore(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 检查 GET/POST 参数
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            for (String val : entry.getValue()) {
                List<String> hits = XssUtils.detect(val, properties.getCustomPatterns());
                if (!hits.isEmpty()) {
                    handleBlock(response, request, entry.getKey(), hits);
                    return;
                }
            }
        }

        // 检查 JSON body（需包装请求以支持下游再次读取 body）
        HttpServletRequest requestToUse = request;
        if (isJsonRequest(request)) {
            byte[] bodyBytes = StreamUtils.copyToByteArray(request.getInputStream());
            if (bodyBytes.length > 0) {
                String body = new String(bodyBytes, StandardCharsets.UTF_8);
                List<String> hits = detectInJson(body);
                if (!hits.isEmpty()) {
                    handleBlock(response, request, "body", hits);
                    return;
                }
            }
            requestToUse = new CachedBodyRequestWrapper(request, bodyBytes);
        }

        filterChain.doFilter(requestToUse, response);
    }

    /**
     * 缓存请求体的包装器
     *
     * @since 2026/03/06
     */
    private static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        CachedBodyRequestWrapper(HttpServletRequest request, byte[] body) {
            super(request);
            this.cachedBody = body != null ? body : new byte[0];
        }

        @Override
        public ServletInputStream getInputStream() {
            return new ServletInputStream() {
                private final ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);

                @Override
                public boolean isFinished() {
                    return bais.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public int read() {
                    return bais.read();
                }
            };
        }
    }

    /**
     * 检查是否忽略
     *
     * @param request 请求
     * @return 是否忽略
     * @since 2026/03/06
     */
    private boolean ignore(HttpServletRequest request) {
        List<String> ignoreUrls = properties.getIgnoreUrls();
        if (ignoreUrls == null || ignoreUrls.isEmpty()) {
            return false;
        }
        String path = request.getRequestURI();
        return ignoreUrls.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    /**
     * 检测 JSON 中的 XSS
     *
     * @param json JSON 字符串
     * @return 检测到的模式列表
     * @since 2026/03/06
     */
    private List<String> detectInJson(String json) {
        List<String> hits = new ArrayList<>();
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            traverse(obj, hits);
        } catch (Exception e) {
            hits.addAll(XssUtils.detect(json, properties.getCustomPatterns()));
        }
        return hits;
    }

    /**
     * 遍历对象检测 XSS
     *
     * @param obj  对象
     * @param hits 检测结果
     * @since 2026/03/06
     */
    private void traverse(Object obj, List<String> hits) {
        if (obj == null) {
            return;
        }
        if (obj instanceof CharSequence) {
            hits.addAll(XssUtils.detect(obj.toString(), properties.getCustomPatterns()));
            return;
        }
        if (obj instanceof Map<?, ?>) {
            ((Map<?, ?>) obj).values().forEach(v -> traverse(v, hits));
        } else if (obj instanceof Iterable<?>) {
            ((Iterable<?>) obj).forEach(v -> traverse(v, hits));
        }
    }

    /**
     * 判断是否为 JSON 请求
     *
     * @param request 请求
     * @return 是否为 JSON
     * @since 2026/03/06
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
     * @param hits     检测到的模式
     * @throws IOException IO 异常
     * @since 2026/03/06
     */
    private void handleBlock(HttpServletResponse response, HttpServletRequest request,
                            String param, List<String> hits) throws IOException {
        log.warn("XSS 脚本拦截: url={}, ip={}, param={}, patterns={}",
                request.getRequestURI(), getClientIP(request), param, hits);

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        R<Void> result = R.error(GlobalMsgCode.BAD_REQUEST, "请求参数包含非法脚本");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * 获取客户端 IP
     *
     * @param request 请求
     * @return 客户端 IP
     * @since 2026/03/06
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
