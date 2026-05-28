package io.github.genkidoudou.report.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * 积木报表前端在 iframe 内部发起的 XHR 往往不会带上自定义 token 参数/请求头，
 * 但 Sa-Token 登录态 token 通常存在于 Header/Cookie（token-name=Authorization）。
 * <p>
 * 该 Filter 在进入积木拦截器前，把 Authorization token 透传为积木识别的 Header：
 * token / X-Access-Token，避免出现 “Token校验失败！请求无权限”。
 */
public class JimuTokenHeaderBridgeFilter extends OncePerRequestFilter {

    private static final String TOKEN_NAME = "Authorization";
    private static final String COOKIE_ADMIN_TOKEN = "Admin-Token";
    private static final String HEADER_TOKEN = "token";
    private static final String HEADER_X_ACCESS_TOKEN = "X-Access-Token";
    private static final int COOKIE_MAX_AGE_SECONDS = 7 * 24 * 60 * 60;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null
                || !(uri.startsWith("/jmreport/") || uri.startsWith("/jmreport")
                || uri.startsWith("/drag/") || uri.startsWith("/drag"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 1) iframe 首跳 URL 会带 token=xxx（quick-ui 注入）；后续 XHR 常丢失 token 参数
        // 2) quick-ui 登录态 cookie 为 Admin-Token=xxx
        // 3) Sa-Token 配置 token-name=Authorization，可能同时存在 Cookie/Header
        // 4) 积木自身也会发 X-Access-Token，但可能与宿主登录态不一致；以宿主为准
        String token = firstNonBlank(
                request.getParameter("token"),
                getCookieValue(request, COOKIE_ADMIN_TOKEN),
                getCookieValue(request, TOKEN_NAME),
                stripBearer(request.getHeader(TOKEN_NAME)),
                request.getHeader(HEADER_TOKEN),
                request.getHeader(HEADER_X_ACCESS_TOKEN)
        );
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 关键：把 token 写入 Cookie，确保 iframe 内部后续 XHR（/jmreport/**）也能携带登录态
        //（quick-ui 的 iframeUrl 会把 token 拼到 /jmreport/list 的 URL 上，但后续 XHR 不会带 token 参数）
        ensureAuthorizationCookie(response, token);

        HttpServletRequest wrapped = new HeaderOverrideRequestWrapper(request, Map.of(
                HEADER_TOKEN, token,
                HEADER_X_ACCESS_TOKEN, token,
                TOKEN_NAME, BEARER_PREFIX + token
        ));
        filterChain.doFilter(wrapped, response);
    }

    private static String getCookieValue(HttpServletRequest request, String name) {
        if (request == null || name == null) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (c != null && name.equalsIgnoreCase(c.getName())) {
                String v = stripBearer(c.getValue());
                if (notBlank(v)) {
                    return v;
                }
            }
        }
        return null;
    }

    private static void ensureAuthorizationCookie(HttpServletResponse response, String token) {
        Cookie c = new Cookie(TOKEN_NAME, token);
        c.setPath("/");
        c.setHttpOnly(false);
        c.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        response.addCookie(c);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (notBlank(v)) {
                return v;
            }
        }
        return null;
    }

    private static String stripBearer(String v) {
        if (v == null) {
            return null;
        }
        String s = v.trim();
        if (s.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return s.substring(7).trim();
        }
        return s;
    }

    private static boolean notBlank(String v) {
        return v != null && !v.trim().isEmpty();
    }

    private static final class HeaderOverrideRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String> overrides;

        private HeaderOverrideRequestWrapper(HttpServletRequest request, Map<String, String> overrides) {
            super(request);
            Map<String, String> lower = new HashMap<>();
            overrides.forEach((k, v) -> lower.put(k.toLowerCase(Locale.ROOT), v));
            this.overrides = Collections.unmodifiableMap(lower);
        }

        @Override
        public String getHeader(String name) {
            if (name != null) {
                String v = overrides.get(name.toLowerCase(Locale.ROOT));
                if (v != null) {
                    return v;
                }
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String v = getHeader(name);
            if (v == null) {
                return super.getHeaders(name);
            }
            return Collections.enumeration(List.of(v));
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<>();
            Enumeration<String> base = super.getHeaderNames();
            while (base.hasMoreElements()) {
                names.add(base.nextElement());
            }
            for (String k : overrides.keySet()) {
                names.add(k);
            }
            return Collections.enumeration(names);
        }
    }
}

