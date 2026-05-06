package io.github.genkidoudou.common.security.firewall.sensitiveword;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.servlet.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.util.List;

/**
 * 敏感词防火墙：尽早于链上处理 query/form 与 {@code application/json} body；THROW 时写出 {@link HttpCodes#SENSITIVE_WORD}。
 */
public class SensitiveWordFirewallFilter extends OncePerRequestFilter {

    private final SensitiveWordFirewallProperties properties;
    private final SensitiveWordEngine engine;
    private final SensitiveWordJsonBodyProcessor jsonProcessor;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    public SensitiveWordFirewallFilter(SensitiveWordFirewallProperties properties,
                                       SensitiveWordEngine engine,
                                       ObjectMapper objectMapper) {
        this.properties = properties;
        this.engine = engine;
        this.jsonProcessor = new SensitiveWordJsonBodyProcessor(objectMapper);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String pathWithinApp = urlPathHelper.getPathWithinApplication(request);
        if (matchesAny(pathWithinApp, properties.getIgnoreUrls())) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
            byte[] outBody = body;
            if (isJsonRequest(request) && body.length > 0) {
                outBody = jsonProcessor.transform(body, engine, properties.getStrategy());
            }
            SensitiveWordHttpServletRequestWrapper wrapped = new SensitiveWordHttpServletRequestWrapper(
                    request, outBody, engine, properties.getStrategy());
            filterChain.doFilter(wrapped, response);
        } catch (SensitiveWordException ex) {
            ServletUtils.writeResponse(response, HttpCodes.SENSITIVE_WORD, ex.getHitWord());
        }
    }

    private static boolean isJsonRequest(HttpServletRequest request) {
        String ct = request.getContentType();
        if (ct == null || ct.isEmpty()) {
            return false;
        }
        try {
            return MediaType.parseMediaType(ct).isCompatibleWith(MediaType.APPLICATION_JSON);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean matchesAny(String path, List<String> patterns) {
        if (path == null || patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String p : patterns) {
            if (p == null || p.isEmpty()) {
                continue;
            }
            if (antPathMatcher.match(p, path)) {
                return true;
            }
        }
        return false;
    }
}
