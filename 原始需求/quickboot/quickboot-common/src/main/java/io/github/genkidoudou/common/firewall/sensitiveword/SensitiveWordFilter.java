package io.github.genkidoudou.common.firewall.sensitiveword;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.core.R;
import io.github.genkidoudou.common.utils.ServletUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤器
 * <p>
 * 拦截 HTTP 请求，对请求参数和请求体进行敏感词过滤
 * 支持表单请求和 JSON 请求
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
@Slf4j
@RequiredArgsConstructor
public class SensitiveWordFilter extends OncePerRequestFilter {

    /**
     * 敏感词服务
     *
     * @since 2026/03/02
     */
    private final SensitiveWordService sensitiveWordService;

    /**
     * 敏感词配置属性
     *
     * @since 2026/03/02
     */
    private final SensitiveWordProperties properties;

    /**
     * JSON 对象映射器
     *
     * @since 2026/03/02
     */
    private final ObjectMapper objectMapper;

    /**
     * 路径匹配器
     *
     * @since 2026/03/02
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 1. 检查是否需要过滤
        if (!needFilter(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. 包装请求
        SensitiveWordRequestWrapper wrapper = new SensitiveWordRequestWrapper(
                httpRequest, sensitiveWordService, objectMapper);

        // 3. 继续过滤链
        try {
            chain.doFilter(wrapper, response);
        } catch (SensitiveWordException e) {
            log.error("检测到敏感词:{}", e);
            ServletUtils.writeResponse(response, e.getCode());
            return;
        }
    }

    /**
     * 判断是否需要过滤
     *
     * @param request HTTP 请求
     * @return 是否需要过滤
     * @since 2026/03/02
     */
    private boolean needFilter(HttpServletRequest request) {
        // 1. 检查是否启用
        if (!properties.getEnable()) {
            return false;
        }

        // 2. 检查是否在忽略列表中
        String requestUri = request.getRequestURI();
        for (String ignoreUrl : properties.getIgnoreUrls()) {
            if (pathMatcher.match(ignoreUrl, requestUri)) {
                log.debug("请求 {} 在忽略列表中，跳过敏感词过滤", requestUri);
                return false;
            }
        }

        return true;
    }

    /**
     * 敏感词请求包装器
     * <p>
     * 包装 HttpServletRequest，对请求参数和请求体进行敏感词过滤
     *
     * @since 2026/03/02
     */
    static class SensitiveWordRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {

        /**
         * 敏感词服务
         *
         * @since 2026/03/02
         */
        private final SensitiveWordService sensitiveWordService;

        /**
         * JSON 对象映射器
         *
         * @since 2026/03/02
         */
        private final ObjectMapper objectMapper;

        /**
         * 缓存的请求体
         *
         * @since 2026/03/02
         */
        private byte[] cachedBody;

        /**
         * 构造函数
         *
         * @param request              HTTP 请求
         * @param sensitiveWordService 敏感词服务
         * @param objectMapper         JSON 对象映射器
         * @throws IOException IO 异常
         * @since 2026/03/02
         */
        public SensitiveWordRequestWrapper(HttpServletRequest request,
                                           SensitiveWordService sensitiveWordService,
                                           ObjectMapper objectMapper) throws IOException {
            super(request);
            this.sensitiveWordService = sensitiveWordService;
            this.objectMapper = objectMapper;

            // 缓存请求体
            if (request.getInputStream() != null) {
                this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());

                // 处理 JSON 请求体
                if (isJsonRequest(request)) {
                    processJsonBody();
                }
            }
        }

        /**
         * 判断是否为 JSON 请求
         *
         * @param request HTTP 请求
         * @return 是否为 JSON 请求
         * @since 2026/03/02
         */
        private boolean isJsonRequest(HttpServletRequest request) {
            String contentType = request.getContentType();
            return contentType != null && contentType.contains("application/json");
        }

        /**
         * 处理 JSON 请求体
         *
         * @throws IOException IO 异常
         * @since 2026/03/02
         */
        private void processJsonBody() throws IOException {
            if (cachedBody == null || cachedBody.length == 0) {
                return;
            }


            // 1. 解析 JSON
            String bodyStr = new String(cachedBody, StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = objectMapper.readValue(bodyStr, Map.class);

            // 2. 过滤敏感词
            Map<String, Object> filteredMap = filterMap(bodyMap);

            // 3. 重新序列化
            String filteredBodyStr = objectMapper.writeValueAsString(filteredMap);
            cachedBody = filteredBodyStr.getBytes(StandardCharsets.UTF_8);
        }

        /**
         * 递归过滤 Map 中的敏感词
         *
         * @param map 待过滤的 Map
         * @return 过滤后的 Map
         * @since 2026/03/02
         */
        @SuppressWarnings("unchecked")
        private Map<String, Object> filterMap(Map<String, Object> map) {
            Map<String, Object> result = new HashMap<>();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof String) {
                    // 字符串类型，进行敏感词过滤
                    result.put(key, sensitiveWordService.process((String) value));
                } else if (value instanceof Map) {
                    // Map 类型，递归过滤
                    result.put(key, filterMap((Map<String, Object>) value));
                } else {
                    // 其他类型，直接保留
                    result.put(key, value);
                }
            }

            return result;
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            if (value != null) {
                return sensitiveWordService.process(value);
            }
            return null;
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values != null) {
                String[] filteredValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    filteredValues[i] = sensitiveWordService.process(values[i]);
                }
                return filteredValues;
            }
            return null;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (cachedBody == null) {
                return super.getInputStream();
            }

            return new CachedBodyServletInputStream(cachedBody);
        }
    }

    /**
     * 缓存的请求体输入流
     *
     * @since 2026/03/02
     */
    static class CachedBodyServletInputStream extends ServletInputStream {

        /**
         * 缓存的请求体
         *
         * @since 2026/03/02
         */
        private final byte[] cachedBody;

        /**
         * 当前读取位置
         *
         * @since 2026/03/02
         */
        private int position = 0;

        /**
         * 构造函数
         *
         * @param cachedBody 缓存的请求体
         * @since 2026/03/02
         */
        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.cachedBody = cachedBody;
        }

        @Override
        public boolean isFinished() {
            return position >= cachedBody.length;
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
        public int read() throws IOException {
            if (isFinished()) {
                return -1;
            }
            return cachedBody[position++] & 0xFF;
        }
    }
}
