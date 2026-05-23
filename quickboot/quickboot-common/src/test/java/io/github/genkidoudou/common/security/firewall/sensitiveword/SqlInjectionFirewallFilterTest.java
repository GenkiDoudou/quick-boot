package io.github.genkidoudou.common.security.firewall.sensitiveword;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.security.firewall.sqlinjection.SqlInjectionFirewallFilter;
import io.github.genkidoudou.common.security.firewall.sqlinjection.SqlInjectionFirewallProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SqlInjectionFirewallFilter} 与敏感词 Filter 链式行为单测（同包以使用 {@link SensitiveWordEngine#wrapForTests}）。
 */
class SqlInjectionFirewallFilterTest {

    private static SensitiveWordEngine smallEngine() {
        SensitiveWordBs bs = SensitiveWordBs.newInstance()
                .wordDeny(() -> List.of("badword"))
                .wordAllow(Collections::emptyList)
                .init();
        return SensitiveWordEngine.wrapForTests(bs);
    }

    @Test
    void ignoreUrls_skipsDetection() throws Exception {
        SqlInjectionFirewallProperties props = new SqlInjectionFirewallProperties();
        props.setIgnoreUrls(List.of("/pub/**"));
        SqlInjectionFirewallFilter filter = new SqlInjectionFirewallFilter(props, new ObjectMapper());

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/pub/x");
        req.setContextPath("");
        req.addParameter("q", "union select");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(req, resp, chain);

        assertThat(resp.getContentAsString(StandardCharsets.UTF_8)).isEmpty();
        assertThat(chain.getRequest()).isSameAs(req);
    }

    @Test
    void query_union_blocked() throws Exception {
        SqlInjectionFirewallProperties props = new SqlInjectionFirewallProperties();
        SqlInjectionFirewallFilter filter = new SqlInjectionFirewallFilter(props, new ObjectMapper());

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/search");
        req.setContextPath("");
        req.addParameter("q", "1 union select");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, new MockFilterChain());

        var root = new ObjectMapper().readTree(resp.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.SQL_INJECTION_DETECTED);
    }

    @Test
    void json_apiPathPatterns_antWildcard_notBlocked() throws Exception {
        SqlInjectionFirewallProperties props = new SqlInjectionFirewallProperties();
        SqlInjectionFirewallFilter filter = new SqlInjectionFirewallFilter(props, new ObjectMapper());

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/system/oauthClient/update");
        req.setContextPath("");
        req.setContentType("application/json;charset=UTF-8");
        req.setContent("""
                {"clientId":"quick-ui","apiPathPatterns":"/system/**\\n/monitor/**","clientSecret":""}
                """.strip().getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(req, resp, chain);

        assertThat(resp.getContentAsString(StandardCharsets.UTF_8)).isEmpty();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void json_nested_blocked() throws Exception {
        SqlInjectionFirewallProperties props = new SqlInjectionFirewallProperties();
        SqlInjectionFirewallFilter filter = new SqlInjectionFirewallFilter(props, new ObjectMapper());

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        req.setContextPath("");
        req.setContentType("application/json;charset=UTF-8");
        req.setContent("{\"a\":{\"b\":\"x union select y\"}}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, new MockFilterChain());

        var root = new ObjectMapper().readTree(resp.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.SQL_INJECTION_DETECTED);
    }

    @Test
    void chained_before_sensitiveWord_jsonHitsSqlFirst() throws Exception {
        SqlInjectionFirewallProperties sqlProps = new SqlInjectionFirewallProperties();
        SqlInjectionFirewallFilter sqlFilter = new SqlInjectionFirewallFilter(sqlProps, new ObjectMapper());

        SensitiveWordFirewallProperties sensProps = new SensitiveWordFirewallProperties();
        sensProps.setStrategy(SensitiveWordFirewallStrategy.THROW);
        SensitiveWordFirewallFilter sensFilter = new SensitiveWordFirewallFilter(sensProps, smallEngine(), new ObjectMapper());

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        req.setContextPath("");
        req.setContentType("application/json");
        req.setContent("{\"msg\":\"union select\"}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = chainThen(sensFilter);
        sqlFilter.doFilter(req, resp, chain);

        var root = new ObjectMapper().readTree(resp.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.SQL_INJECTION_DETECTED);
    }

    @Test
    void chained_before_sensitiveWord_cleanSql_sensitiveCatches() throws Exception {
        SqlInjectionFirewallProperties sqlProps = new SqlInjectionFirewallProperties();
        SqlInjectionFirewallFilter sqlFilter = new SqlInjectionFirewallFilter(sqlProps, new ObjectMapper());

        SensitiveWordFirewallProperties sensProps = new SensitiveWordFirewallProperties();
        sensProps.setStrategy(SensitiveWordFirewallStrategy.THROW);
        SensitiveWordFirewallFilter sensFilter = new SensitiveWordFirewallFilter(sensProps, smallEngine(), new ObjectMapper());

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        req.setContextPath("");
        req.setContentType("application/json");
        req.setContent("{\"msg\":\"badword\"}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = chainThen(sensFilter);
        sqlFilter.doFilter(req, resp, chain);

        var root = new ObjectMapper().readTree(resp.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.SENSITIVE_WORD);
    }

    private static FilterChain chainThen(SensitiveWordFirewallFilter sensFilter) {
        return (request, response) -> {
            try {
                sensFilter.doFilter((HttpServletRequest) request, (HttpServletResponse) response, new MockFilterChain());
            } catch (ServletException e) {
                throw new IOException(e);
            }
        };
    }
}
