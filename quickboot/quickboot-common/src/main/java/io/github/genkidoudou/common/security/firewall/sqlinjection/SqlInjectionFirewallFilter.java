package io.github.genkidoudou.common.security.firewall.sqlinjection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.servlet.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * SQL 注入启发式防火墙：对 Query/Form 与 JSON 字符串做关键字子串检测；早于敏感词 Filter 执行并对原始输入校验。
 * <p>
 * 对 {@code application/json}：若 JSON <strong>解析失败</strong>，本 Filter <strong>不因 SQL 能力拦截</strong>请求，
 * 仍缓存 body 供下游处理（与 design 约定一致）。
 * </p>
 */
public class SqlInjectionFirewallFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SqlInjectionFirewallFilter.class);

    private final SqlInjectionFirewallProperties properties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    /**
     * @param properties    配置，不可为 {@code null}
     * @param objectMapper  Jackson，不可为 {@code null}
     */
    public SqlInjectionFirewallFilter(SqlInjectionFirewallProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
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

        List<String> keywords = effectiveKeywords();
        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());

        SqlHit queryHit = scanParameters(request, keywords);
        if (queryHit != null) {
            block(request, response, queryHit);
            return;
        }

        if (isJsonRequest(request) && body.length > 0) {
            try {
                JsonNode tree = objectMapper.readTree(body);
                SqlHit jsonHit = scanJsonTree(tree, "body", keywords);
                if (jsonHit != null) {
                    block(request, response, jsonHit);
                    return;
                }
            } catch (JsonProcessingException ex) {
                // 解析失败：不按 SQL 关键字拦截，避免误杀非 JSON；body 仍缓存向下游传递
                log.trace("sql-injection: skip JSON scan, parse failed: {}", ex.toString());
            }
        }

        HttpServletRequest nextRequest = new CachedBodyHttpServletRequestWrapper(request, body);
        filterChain.doFilter(nextRequest, response);
    }

    private List<String> effectiveKeywords() {
        List<String> cfg = properties.getKeywords();
        if (cfg != null && !cfg.isEmpty()) {
            List<String> out = new ArrayList<>();
            for (String s : cfg) {
                if (s == null || s.isEmpty()) {
                    continue;
                }
                out.add(s.toLowerCase(Locale.ROOT));
            }
            return out.isEmpty() ? SqlInjectionFirewallKeywordDefaults.DEFAULT_KEYWORDS : out;
        }
        return SqlInjectionFirewallKeywordDefaults.DEFAULT_KEYWORDS;
    }

    private SqlHit scanParameters(HttpServletRequest request, List<String> keywords) {
        for (Map.Entry<String, String[]> e : request.getParameterMap().entrySet()) {
            String name = e.getKey();
            String[] values = e.getValue();
            if (values == null) {
                continue;
            }
            for (String val : values) {
                Set<String> hits = matchKeywords(val, keywords);
                if (!hits.isEmpty()) {
                    return new SqlHit(hits, "query." + name);
                }
            }
        }
        return null;
    }

    private SqlHit scanJsonTree(JsonNode node, String path, List<String> keywords) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            for (var it = obj.fieldNames(); it.hasNext(); ) {
                String name = it.next();
                SqlHit h = scanJsonTree(obj.get(name), path + "." + name, keywords);
                if (h != null) {
                    return h;
                }
            }
            return null;
        }
        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                SqlHit h = scanJsonTree(node.get(i), path + "[" + i + "]", keywords);
                if (h != null) {
                    return h;
                }
            }
            return null;
        }
        if (node.isTextual()) {
            Set<String> hits = matchKeywords(node.asText(), keywords);
            if (!hits.isEmpty()) {
                return new SqlHit(hits, path);
            }
        }
        return null;
    }

    private static Set<String> matchKeywords(String value, List<String> keywordsLower) {
        if (value == null || value.isEmpty()) {
            return Set.of();
        }
        String haystack = value.toLowerCase(Locale.ROOT);
        Set<String> matched = new LinkedHashSet<>();
        for (String needle : keywordsLower) {
            if (needle.isEmpty()) {
                continue;
            }
            if (haystack.contains(needle)) {
                matched.add(needle);
            }
        }
        return matched;
    }

    private void block(HttpServletRequest request, HttpServletResponse response, SqlHit hit)
            throws IOException {
        log.warn(
                "sql-injection blocked: method={} path={} ip={} context={} keywords={}",
                request.getMethod(),
                urlPathHelper.getPathWithinApplication(request),
                request.getRemoteAddr(),
                hit.context(),
                hit.keywords());
        ServletUtils.writeResponse(response, HttpCodes.SQL_INJECTION_DETECTED, properties.getForbiddenMessage());
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

    private record SqlHit(Set<String> keywords, String context) {
    }
}
