package io.github.genkidoudou.common.security.firewall.methodandhost;

import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.servlet.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * 安全防火墙：在进入业务逻辑前拦截不允许的 HTTP Method 与 Host（基于 {@code Host} 请求头）。
 */
public class MethodAndHostFirewallFilter extends OncePerRequestFilter {

    private final MethodAndHostFirewallProperties properties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    public MethodAndHostFirewallFilter(MethodAndHostFirewallProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String pathWithinApp = urlPathHelper.getPathWithinApplication(request);
        if (matchesAny(pathWithinApp, properties.getExcludeUrls())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!isMethodAllowed(request.getMethod(), properties.getAllowedMethods())) {
            ServletUtils.writeResponse(response, HttpCodes.METHOD_NOT_ALLOWED, properties.getForbiddenMessage());
            return;
        }

        if (!isHostAllowed(request, properties.getAllowedHosts())) {
            ServletUtils.writeResponse(response, HttpCodes.HOST_NOT_ALLOWED, properties.getForbiddenMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isMethodAllowed(String method, List<String> allowedMethods) {
        if (allowedMethods == null || allowedMethods.isEmpty()) {
            return true;
        }
        String m = (method == null ? "" : method).toUpperCase(Locale.ROOT);
        for (String allow : allowedMethods) {
            if (allow == null || allow.isBlank()) {
                continue;
            }
            if (m.equalsIgnoreCase(allow.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isHostAllowed(HttpServletRequest request, List<String> allowedHosts) {
        if (allowedHosts == null || allowedHosts.isEmpty()) {
            return true;
        }
        String rawHost = request.getHeader("Host");
        if (rawHost == null || rawHost.isBlank()) {
            return false;
        }

        HostPort hp = parseRequestHost(rawHost.trim(), request.getServerPort());
        if (hp == null || hp.host == null || hp.host.isBlank() || hp.port <= 0) {
            return false;
        }
        String normalized = hp.host + ":" + hp.port;

        for (String p : allowedHosts) {
            AllowedHostPattern pattern = parseAllowedHostPattern(p);
            if (pattern == null) {
                continue;
            }
            if (!pattern.matches(normalized)) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * 解析请求侧 Host 头并规范化（lower-case + 缺省端口补齐 + IPv6 {@code []} 支持）。
     */
    private static HostPort parseRequestHost(String raw, int defaultPort) {
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.startsWith("[")) {
            int close = lower.indexOf(']');
            if (close <= 0) {
                return null;
            }
            String host = lower.substring(0, close + 1);
            int port = defaultPort;
            if (close + 1 < lower.length()) {
                if (lower.charAt(close + 1) != ':') {
                    return null;
                }
                String p = lower.substring(close + 2).trim();
                Integer parsed = parsePort(p);
                if (parsed == null) {
                    return null;
                }
                port = parsed;
            }
            return new HostPort(host, port);
        }

        int firstColon = lower.indexOf(':');
        int lastColon = lower.lastIndexOf(':');
        if (firstColon != -1 && firstColon != lastColon) {
            // 多个冒号但未使用 [] 的情况视为非法（避免误把 IPv6 拆分）
            return null;
        }
        String host = lower;
        int port = defaultPort;
        if (lastColon != -1) {
            host = lower.substring(0, lastColon).trim();
            String p = lower.substring(lastColon + 1).trim();
            Integer parsed = parsePort(p);
            if (parsed == null) {
                return null;
            }
            port = parsed;
        }
        return new HostPort(host, port);
    }

    private static Integer parsePort(String p) {
        if (p == null || p.isBlank()) {
            return null;
        }
        try {
            int v = Integer.parseInt(p);
            return v > 0 ? v : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private AllowedHostPattern parseAllowedHostPattern(String rawPattern) {
        if (rawPattern == null || rawPattern.isBlank()) {
            return null;
        }
        String lower = rawPattern.trim().toLowerCase(Locale.ROOT);

        String host;
        String portPattern = "*";

        if (lower.startsWith("[")) {
            int close = lower.indexOf(']');
            if (close <= 0) {
                return null;
            }
            host = lower.substring(0, close + 1);
            if (close + 1 < lower.length()) {
                if (lower.charAt(close + 1) != ':') {
                    return null;
                }
                portPattern = lower.substring(close + 2).trim();
            }
        } else {
            int firstColon = lower.indexOf(':');
            int lastColon = lower.lastIndexOf(':');
            if (firstColon != -1 && firstColon != lastColon) {
                return null;
            }
            if (lastColon == -1) {
                host = lower.trim();
            } else {
                host = lower.substring(0, lastColon).trim();
                portPattern = lower.substring(lastColon + 1).trim();
            }
        }

        if (host.isBlank()) {
            return null;
        }
        if (portPattern.isBlank()) {
            portPattern = "*";
        }

        return new AllowedHostPattern(host, portPattern);
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

    private record HostPort(String host, int port) {
    }

    private static final class AllowedHostPattern {
        private final String hostPattern;
        private final String portPattern;
        private final boolean wildcardSubdomain;
        private final String wildcardBase;

        private AllowedHostPattern(String hostPattern, String portPattern) {
            this.hostPattern = hostPattern;
            this.portPattern = portPattern;
            this.wildcardSubdomain = hostPattern.startsWith("*.");
            this.wildcardBase = this.wildcardSubdomain ? hostPattern.substring(2) : null;
        }

        boolean matches(String normalizedHostPort) {
            HostPort hp = splitNormalized(normalizedHostPort);
            if (hp == null) {
                return false;
            }
            if (!matchesPort(hp.port)) {
                return false;
            }
            return matchesHost(hp.host);
        }

        private boolean matchesPort(int port) {
            if ("*".equals(portPattern)) {
                return true;
            }
            try {
                return Integer.parseInt(portPattern) == port;
            } catch (Exception ignored) {
                return false;
            }
        }

        private boolean matchesHost(String host) {
            if (wildcardSubdomain) {
                if (wildcardBase == null || wildcardBase.isBlank()) {
                    return false;
                }
                if (host.equals(wildcardBase)) {
                    return false;
                }
                return host.endsWith("." + wildcardBase);
            }
            return hostPattern.equals(host);
        }

        private static HostPort splitNormalized(String normalized) {
            if (normalized == null || normalized.isBlank()) {
                return null;
            }
            String lower = normalized.toLowerCase(Locale.ROOT);
            if (lower.startsWith("[")) {
                int close = lower.indexOf(']');
                if (close <= 0 || close + 2 > lower.length() || lower.charAt(close + 1) != ':') {
                    return null;
                }
                String host = lower.substring(0, close + 1);
                Integer port = parsePort(lower.substring(close + 2));
                return port == null ? null : new HostPort(host, port);
            }
            int idx = lower.lastIndexOf(':');
            if (idx <= 0 || idx == lower.length() - 1) {
                return null;
            }
            String host = lower.substring(0, idx);
            Integer port = parsePort(lower.substring(idx + 1));
            return port == null ? null : new HostPort(host, port);
        }
    }
}

