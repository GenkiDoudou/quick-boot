package io.github.genkidoudou.common.security.firewall.xss;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.security.firewall.sqlinjection.CachedBodyHttpServletRequestWrapper;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * XSS 启发式防火墙：Query/Form、JSON 字符串、{@code multipart/form-data} 无 {@code filename} 文本字段；
 * 早于 SQL 注入（+4）与敏感词（+5）；未命中则缓存 body 向下游传递。
 */
public class XssFirewallFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(XssFirewallFilter.class);

    private final XssFirewallProperties properties;
    private final XssFirewallRuleSet ruleSet;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    /**
     * @param properties 配置
     * @param ruleSet    已编译规则（构造失败表示配置非法）
     * @param objectMapper Jackson
     */
    public XssFirewallFilter(XssFirewallProperties properties,
                            XssFirewallRuleSet ruleSet,
                            ObjectMapper objectMapper) {
        this.properties = properties;
        this.ruleSet = ruleSet;
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

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());

        XssHit paramHit = scanParameters(request);
        if (paramHit != null) {
            block(request, response, paramHit);
            return;
        }

        if (isMultipartRequest(request) && body.length > 0) {
            for (MultipartFormDataTextParts.NamedText nt : MultipartFormDataTextParts.extract(body, request.getContentType())) {
                Optional<XssFirewallRuleSet.XssMatch> m = ruleSet.matchFirst(nt.text());
                if (m.isPresent()) {
                    block(request, response, new XssHit(m.get(), "multipart." + nt.name()));
                    return;
                }
            }
        } else if (isJsonRequest(request) && body.length > 0) {
            try {
                JsonNode tree = objectMapper.readTree(body);
                XssHit jsonHit = scanJsonTree(tree, "body");
                if (jsonHit != null) {
                    block(request, response, jsonHit);
                    return;
                }
            } catch (JsonProcessingException ex) {
                log.trace("xss-firewall: skip JSON scan, parse failed: {}", ex.toString());
            }
        }

        HttpServletRequest next = new CachedBodyHttpServletRequestWrapper(request, body);
        filterChain.doFilter(next, response);
    }

    private XssHit scanParameters(HttpServletRequest request) {
        for (Map.Entry<String, String[]> e : request.getParameterMap().entrySet()) {
            String name = e.getKey();
            String[] values = e.getValue();
            if (values == null) {
                continue;
            }
            for (String val : values) {
                Optional<XssFirewallRuleSet.XssMatch> m = ruleSet.matchFirst(val);
                if (m.isPresent()) {
                    return new XssHit(m.get(), "query." + name);
                }
            }
        }
        return null;
    }

    private XssHit scanJsonTree(JsonNode node, String path) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            for (var it = obj.fieldNames(); it.hasNext(); ) {
                String name = it.next();
                XssHit h = scanJsonTree(obj.get(name), path + "." + name);
                if (h != null) {
                    return h;
                }
            }
            return null;
        }
        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                XssHit h = scanJsonTree(node.get(i), path + "[" + i + "]");
                if (h != null) {
                    return h;
                }
            }
            return null;
        }
        if (node.isTextual()) {
            Optional<XssFirewallRuleSet.XssMatch> m = ruleSet.matchFirst(node.asText());
            return m.map(xssMatch -> new XssHit(xssMatch, path)).orElse(null);
        }
        return null;
    }

    private void block(HttpServletRequest request, HttpServletResponse response, XssHit hit) throws IOException {
        log.warn(
                "xss-firewall blocked: method={} path={} ip={} context={} rule={}",
                request.getMethod(),
                urlPathHelper.getPathWithinApplication(request),
                request.getRemoteAddr(),
                hit.context(),
                hit.match().ruleId());
        ServletUtils.writeResponse(response, HttpCodes.XSS_SCRIPT_DETECTED, properties.getForbiddenMessage());
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

    private static boolean isMultipartRequest(HttpServletRequest request) {
        String ct = request.getContentType();
        if (ct == null || ct.isEmpty()) {
            return false;
        }
        try {
            return MediaType.parseMediaType(ct).isCompatibleWith(MediaType.MULTIPART_FORM_DATA);
        } catch (Exception ignored) {
            return false;
        }
    }

    private record XssHit(XssFirewallRuleSet.XssMatch match, String context) {
    }
}
