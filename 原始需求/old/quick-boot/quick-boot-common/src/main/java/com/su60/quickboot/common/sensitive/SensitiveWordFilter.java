//package com.su60.quickboot.common.sensitive;
//
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
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
///**
// * 敏感词过滤器，处理参数与 JSON Body。
// */
//@Slf4j
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE + 1)
//@RequiredArgsConstructor
//@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "security.sensitive-word", name = "enabled", havingValue = "true", matchIfMissing = false)
//public class SensitiveWordFilter extends OncePerRequestFilter {
//
//    private final SecurityProperties securityProperties;
//    private final SensitiveWordService sensitiveWordService;
//    private final AntPathMatcher pathMatcher = new AntPathMatcher();
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        SecurityProperties.SensitiveWordProperties cfg = securityProperties.getSensitiveWord();
//        if (!Boolean.TRUE.equals(cfg.getEnabled())) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        if (ignore(request, cfg.getIgnoreUrls())) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        try {
//            SensitiveWordRequestWrapper wrapper = new SensitiveWordRequestWrapper(
//                    request, sensitiveWordService, cfg.getStrategy());
//            filterChain.doFilter(wrapper, response);
//        } catch (SensitiveWordException ex) {
//            if (Boolean.TRUE.equals(cfg.getLogEnabled())) {
//                log.warn("敏感词拦截: url={}, ip={}, reason={}", request.getRequestURI(),
//                        ServletUtil.getClientIP(request), ex.getMessage());
//            }
//            writeError(response, ex.getMessage());
//        }
//    }
//
//    private boolean ignore(HttpServletRequest request, List<String> ignores) {
//        String path = request.getRequestURI();
//        return ignores != null && ignores.stream().anyMatch(p -> pathMatcher.match(p, path));
//    }
//
//    private void writeError(HttpServletResponse response, String message) throws IOException {
//        response.setStatus(HttpStatus.OK.value());
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//        response.getWriter().write(JSONUtil.toJsonStr(R.failed(HttpStatus.BAD_REQUEST.value(), message)));
//    }
//}
