package io.github.genkidoudou.common.security.firewall.xss;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link XssFirewallFilter} 行为单测。
 */
class XssFirewallFilterTest {

    private static XssFirewallFilter filter(XssFirewallProperties props) {
        return new XssFirewallFilter(props, new XssFirewallRuleSet(props), new ObjectMapper());
    }

    @Test
    void ignoreUrls_skips() throws Exception {
        XssFirewallProperties props = new XssFirewallProperties();
        props.setIgnoreUrls(List.of("/pub/**"));
        XssFirewallFilter f = filter(props);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/pub/x");
        req.setContextPath("");
        req.addParameter("q", "<script>");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, resp, chain);
        assertThat(resp.getContentAsString(StandardCharsets.UTF_8)).isEmpty();
        assertThat(chain.getRequest()).isSameAs(req);
    }

    @Test
    void query_script_blocked() throws Exception {
        XssFirewallProperties props = new XssFirewallProperties();
        XssFirewallFilter f = filter(props);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/x");
        req.setContextPath("");
        req.addParameter("html", "x <script>");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        f.doFilter(req, resp, new MockFilterChain());
        var root = new ObjectMapper().readTree(resp.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.XSS_SCRIPT_DETECTED);
    }

    @Test
    void json_nested_blocked() throws Exception {
        XssFirewallProperties props = new XssFirewallProperties();
        XssFirewallFilter f = filter(props);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        req.setContextPath("");
        req.setContentType("application/json;charset=UTF-8");
        req.setContent("{\"a\":{\"b\":\"javascript:void(0)\"}}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse resp = new MockHttpServletResponse();
        f.doFilter(req, resp, new MockFilterChain());
        var root = new ObjectMapper().readTree(resp.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.XSS_SCRIPT_DETECTED);
    }

    @Test
    void customPattern_blocked() throws Exception {
        XssFirewallProperties props = new XssFirewallProperties();
        props.setCustomPatterns(List.of("BADTOKEN"));
        XssFirewallFilter f = filter(props);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/x");
        req.setContextPath("");
        req.addParameter("q", "ok BADTOKEN ok");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        f.doFilter(req, resp, new MockFilterChain());
        var root = new ObjectMapper().readTree(resp.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.XSS_SCRIPT_DETECTED);
    }

    @Test
    void multipart_skipsFilePart_scansTextPart() throws Exception {
        XssFirewallProperties props = new XssFirewallProperties();
        XssFirewallFilter f = filter(props);
        String body = ""
                + "--Bb\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"a.bin\"\r\n"
                + "\r\n"
                + "<script>evil</script>\r\n"
                + "--Bb\r\n"
                + "Content-Disposition: form-data; name=\"note\"\r\n"
                + "\r\n"
                + "hi<script\r\n"
                + "--Bb--\r\n";
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/upload");
        req.setContextPath("");
        req.setContentType("multipart/form-data; boundary=Bb");
        req.setContent(body.getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse resp = new MockHttpServletResponse();
        f.doFilter(req, resp, new MockFilterChain());
        var root = new ObjectMapper().readTree(resp.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.XSS_SCRIPT_DETECTED);
    }

    @Test
    void multipart_onlyFilePart_notBlocked() throws Exception {
        XssFirewallProperties props = new XssFirewallProperties();
        XssFirewallFilter f = filter(props);
        String body = ""
                + "--Bb\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"a.bin\"\r\n"
                + "\r\n"
                + "<script>x</script>\r\n"
                + "--Bb--\r\n";
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/upload");
        req.setContextPath("");
        req.setContentType("multipart/form-data; boundary=Bb");
        req.setContent(body.getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, resp, chain);
        assertThat(resp.getContentAsString(StandardCharsets.UTF_8)).isEmpty();
    }

    @Test
    void chained_before_sqlInjection_readableBody() throws Exception {
        XssFirewallProperties xp = new XssFirewallProperties();
        SqlInjectionFirewallProperties sp = new SqlInjectionFirewallProperties();
        XssFirewallFilter xf = filter(xp);
        SqlInjectionFirewallFilter sf = new SqlInjectionFirewallFilter(sp, new ObjectMapper());
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        req.setContextPath("");
        req.setContentType("application/json");
        req.setContent("{\"msg\":\"hello\"}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse resp = new MockHttpServletResponse();
        xf.doFilter(req, resp, chainThen(sf));
        assertThat(resp.getContentAsString(StandardCharsets.UTF_8)).isEmpty();
    }

    @Test
    void invalidCustomPattern_failFast() {
        XssFirewallProperties props = new XssFirewallProperties();
        props.setCustomPatterns(List.of("(unclosed"));
        assertThatThrownBy(() -> new XssFirewallRuleSet(props)).isInstanceOf(IllegalArgumentException.class);
    }

    private static FilterChain chainThen(SqlInjectionFirewallFilter next) {
        return (request, response) -> {
            try {
                next.doFilter((HttpServletRequest) request, (HttpServletResponse) response, new MockFilterChain());
            } catch (ServletException e) {
                throw new IOException(e);
            }
        };
    }
}
