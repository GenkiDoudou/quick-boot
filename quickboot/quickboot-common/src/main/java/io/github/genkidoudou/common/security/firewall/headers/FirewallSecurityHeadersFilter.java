package io.github.genkidoudou.common.security.firewall.headers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.util.List;

/**
 * 在响应提交前按 {@link FirewallHeadersProperties} 注入安全响应头，并支持两段式路径排除。
 */
public class FirewallSecurityHeadersFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(FirewallSecurityHeadersFilter.class);

    static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
    static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    static final String HEADER_X_XSS_PROTECTION = "X-XSS-Protection";
    static final String HEADER_CSP = "Content-Security-Policy";
    static final String HEADER_HSTS = "Strict-Transport-Security";
    static final String HEADER_REFERRER_POLICY = "Referrer-Policy";
    static final String HEADER_PERMISSIONS_POLICY = "Permissions-Policy";

    private static final String DEFAULT_FRAME_OPTIONS = "SAMEORIGIN";
    private static final String DEFAULT_CONTENT_TYPE_OPTIONS = "nosniff";
    private static final String DEFAULT_XSS_PROTECTION = "1; mode=block";
    private static final String DEFAULT_REFERRER_POLICY = "strict-origin-when-cross-origin";

    private final FirewallHeadersProperties properties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    public FirewallSecurityHeadersFilter(FirewallHeadersProperties properties) {
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (response.isCommitted()) {
            log.debug("安全头：响应已提交，跳过写入 path={}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String pathWithinApp = urlPathHelper.getPathWithinApplication(request);

        boolean fullExclude = matchesAny(pathWithinApp, properties.getExcludeUrls());
        boolean strictExclude = matchesAny(pathWithinApp, properties.getExcludeFromStrictPolicyUrls());

        if (fullExclude) {
            filterChain.doFilter(request, response);
            return;
        }

        if (strictExclude) {
            writeBasicHeaders(response);
            filterChain.doFilter(request, response);
            return;
        }

        writeBasicHeaders(response);
        writeStrictHeadersIfConfigured(response);
        filterChain.doFilter(request, response);
    }

    private boolean matchesAny(String path, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (!StringUtils.hasText(pattern)) {
                continue;
            }
            if (antPathMatcher.match(pattern.trim(), path)) {
                return true;
            }
        }
        return false;
    }

    private void writeBasicHeaders(HttpServletResponse response) {
        if (response.isCommitted()) {
            return;
        }
        setHeaderIfWritable(response, HEADER_X_FRAME_OPTIONS,
                textOrDefault(properties.getFrameOptions(), DEFAULT_FRAME_OPTIONS));
        setHeaderIfWritable(response, HEADER_X_CONTENT_TYPE_OPTIONS,
                textOrDefault(properties.getContentTypeOptions(), DEFAULT_CONTENT_TYPE_OPTIONS));
        setHeaderIfWritable(response, HEADER_X_XSS_PROTECTION,
                textOrDefault(properties.getXssProtection(), DEFAULT_XSS_PROTECTION));
        setHeaderIfWritable(response, HEADER_REFERRER_POLICY,
                textOrDefault(properties.getReferrerPolicy(), DEFAULT_REFERRER_POLICY));
    }

    private void writeStrictHeadersIfConfigured(HttpServletResponse response) {
        if (response.isCommitted()) {
            return;
        }
        if (StringUtils.hasText(properties.getContentSecurityPolicy())) {
            setHeaderIfWritable(response, HEADER_CSP, properties.getContentSecurityPolicy().trim());
        }
        if (StringUtils.hasText(properties.getStrictTransportSecurity())) {
            setHeaderIfWritable(response, HEADER_HSTS, properties.getStrictTransportSecurity().trim());
        }
        if (StringUtils.hasText(properties.getPermissionsPolicy())) {
            setHeaderIfWritable(response, HEADER_PERMISSIONS_POLICY, properties.getPermissionsPolicy().trim());
        }
    }

    private static String textOrDefault(String configured, String defaultValue) {
        return StringUtils.hasText(configured) ? configured.trim() : defaultValue;
    }

    private void setHeaderIfWritable(HttpServletResponse response, String name, String value) {
        if (response.isCommitted()) {
            return;
        }
        if (!StringUtils.hasText(value)) {
            return;
        }
        response.setHeader(name, value.trim());
    }
}
