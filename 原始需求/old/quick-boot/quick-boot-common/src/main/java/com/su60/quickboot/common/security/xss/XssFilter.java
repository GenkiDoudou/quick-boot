//package com.su60.quickboot.common.security.xss;
//
//import com.su60.quickboot.common.security.config.SecurityProperties;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//
///**
// * XSS 过滤器。
// */
//@Slf4j
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE + 2)
//@RequiredArgsConstructor
//@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "security.xss", name = "enabled", havingValue = "true", matchIfMissing = false)
//public class XssFilter extends OncePerRequestFilter {
//
//    private final SecurityProperties securityProperties;
//    private final AntPathMatcher pathMatcher = new AntPathMatcher();
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        SecurityProperties.XssProperties cfg = securityProperties.getXss();
//        if (!Boolean.TRUE.equals(cfg.getEnabled())) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        if (ignore(request, cfg.getIgnoreUrls())) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(request);
//        filterChain.doFilter(wrapper, response);
//    }
//
//    private boolean ignore(HttpServletRequest request, List<String> ignores) {
//        String path = request.getRequestURI();
//        String method = request.getMethod();
//        if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
//            return true;
//        }
//        return ignores != null && ignores.stream().anyMatch(p -> pathMatcher.match(p, path));
//    }
//}
