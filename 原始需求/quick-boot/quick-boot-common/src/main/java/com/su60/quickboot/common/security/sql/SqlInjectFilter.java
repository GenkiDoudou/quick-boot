//package com.su60.quickboot.common.security.sql;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.json.JSONUtil;
//import com.su60.quickboot.common.core.R;
//import com.su60.quickboot.common.security.config.SecurityProperties;
//import com.su60.quickboot.common.utils.ServletUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.util.ContentCachingRequestWrapper;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * SQL 注入检测过滤器。
// */
//@Slf4j
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE + 3)
//@RequiredArgsConstructor
//@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "security.sql-inject", name = "enabled", havingValue = "true", matchIfMissing = false)
//public class SqlInjectFilter extends OncePerRequestFilter {
//
//    private final SecurityProperties securityProperties;
//    private final SqlKeywordsProvider sqlKeywordsProvider;
//    private final AntPathMatcher pathMatcher = new AntPathMatcher();
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        SecurityProperties.SqlInjectProperties cfg = securityProperties.getSqlInject();
//        if (!Boolean.TRUE.equals(cfg.getEnabled())) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        if (ignore(request, cfg.getIgnoreUrls())) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);
//
//        // GET params
//        for (Map.Entry<String, String[]> entry : wrapper.getParameterMap().entrySet()) {
//            for (String val : entry.getValue()) {
//                List<String> hits = SqlInjectUtils.detect(val, sqlKeywordsProvider.getKeywords());
//                if (CollUtil.isNotEmpty(hits)) {
//                    handleBlock(response, request, entry.getKey(), hits);
//                    return;
//                }
//            }
//        }
//
//        // JSON body
//        if (isJsonRequest(wrapper)) {
//            // 先读取一次输入流，触发 ContentCachingRequestWrapper 缓存
//            byte[] bodyBytes = wrapper.getInputStream().readAllBytes();
//            // 从缓存中获取内容
//            bodyBytes = wrapper.getContentAsByteArray();
//
//            if (bodyBytes.length > 0) {
//                String body = new String(bodyBytes, StandardCharsets.UTF_8);
//                List<String> hits = detectInJson(body);
//                if (CollUtil.isNotEmpty(hits)) {
//                    handleBlock(response, request, "body", hits);
//                    return;
//                }
//            }
//        }
//
//        filterChain.doFilter(wrapper, response);
//    }
//
//    private boolean ignore(HttpServletRequest request, List<String> ignores) {
//        String path = request.getRequestURI();
//        return ignores != null && ignores.stream().anyMatch(p -> pathMatcher.match(p, path));
//    }
//
//    private List<String> detectInJson(String json) {
//        if (!JSONUtil.isTypeJSON(json)) {
//            return SqlInjectUtils.detect(json, sqlKeywordsProvider.getKeywords());
//        }
//        List<String> hits = new ArrayList<>();
//        Object obj = JSONUtil.parse(json);
//        traverse(obj, hits);
//        return hits;
//    }
//
//    private void traverse(Object obj, List<String> hits) {
//        if (obj == null) {
//            return;
//        }
//        if (obj instanceof CharSequence seq) {
//            hits.addAll(SqlInjectUtils.detect(seq.toString(), sqlKeywordsProvider.getKeywords()));
//            return;
//        }
//        if (obj instanceof Map<?, ?> map) {
//            map.values().forEach(v -> traverse(v, hits));
//        } else if (obj instanceof Iterable<?> it) {
//            it.forEach(v -> traverse(v, hits));
//        }
//    }
//
//    private boolean isJsonRequest(HttpServletRequest request) {
//        String contentType = request.getContentType();
//        return contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);
//    }
//
//    private void handleBlock(HttpServletResponse response, HttpServletRequest request,
//                             String param, List<String> hits) throws IOException {
//        log.warn("SQL注入拦截: url={}, ip={}, param={}, keywords={}",
//                request.getRequestURI(), ServletUtil.getClientIP(request), param, hits);
//        response.setStatus(HttpStatus.BAD_REQUEST.value());
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//        response.getWriter().write(JSONUtil.toJsonStr(R.failed(HttpStatus.BAD_REQUEST.value(), "请求参数包含非法字符")));
//    }
//}
