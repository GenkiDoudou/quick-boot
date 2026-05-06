package io.github.genkidoudou.common.security.firewall.sensitiveword;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 核心行为单测（自定义小词表，避免加载完整默认库过久时可单独运行本类）。
 */
class SensitiveWordFirewallCoreTest {

    private static SensitiveWordEngine smallEngine() {
        SensitiveWordBs bs = SensitiveWordBs.newInstance()
                .wordDeny(() -> List.of("badword"))
                .wordAllow(Collections::emptyList)
                .init();
        return SensitiveWordEngine.wrapForTests(bs);
    }

    @Test
    void json_replace_nested() throws Exception {
        SensitiveWordEngine engine = smallEngine();
        ObjectMapper mapper = new ObjectMapper();
        SensitiveWordJsonBodyProcessor p = new SensitiveWordJsonBodyProcessor(mapper);
        byte[] in = "{\"a\":{\"b\":[\"ok\",\"x badword y\"]}}".getBytes(StandardCharsets.UTF_8);
        byte[] out = p.transform(in, engine, SensitiveWordFirewallStrategy.REPLACE);
        String s = new String(out, StandardCharsets.UTF_8);
        assertThat(s).doesNotContain("badword");
    }

    @Test
    void json_throw() {
        SensitiveWordEngine engine = smallEngine();
        ObjectMapper mapper = new ObjectMapper();
        SensitiveWordJsonBodyProcessor p = new SensitiveWordJsonBodyProcessor(mapper);
        byte[] in = "{\"msg\":\"badword\"}".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> p.transform(in, engine, SensitiveWordFirewallStrategy.THROW))
                .isInstanceOf(SensitiveWordException.class);
    }

    @Test
    void parameter_replace() {
        SensitiveWordEngine engine = smallEngine();
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("q", "hello badword");
        SensitiveWordHttpServletRequestWrapper w = new SensitiveWordHttpServletRequestWrapper(
                req, null, engine, SensitiveWordFirewallStrategy.REPLACE);
        assertThat(w.getParameter("q")).doesNotContain("badword");
    }

    @Test
    void json_replace_preserves_number() throws Exception {
        SensitiveWordEngine engine = smallEngine();
        ObjectMapper mapper = new ObjectMapper();
        SensitiveWordJsonBodyProcessor p = new SensitiveWordJsonBodyProcessor(mapper);
        byte[] in = "{\"n\":42,\"s\":\"x badword y\"}".getBytes(StandardCharsets.UTF_8);
        byte[] out = p.transform(in, engine, SensitiveWordFirewallStrategy.REPLACE);
        var n = mapper.readTree(out);
        assertThat(n.get("n").intValue()).isEqualTo(42);
        assertThat(n.get("s").asText()).doesNotContain("badword");
    }

    @Test
    void filter_throw_writes_json_body() throws Exception {
        SensitiveWordEngine engine = smallEngine();
        SensitiveWordFirewallProperties props = new SensitiveWordFirewallProperties();
        props.setStrategy(SensitiveWordFirewallStrategy.THROW);
        SensitiveWordFirewallFilter filter = new SensitiveWordFirewallFilter(props, engine, new ObjectMapper());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setRequestURI("/api/x");
        req.setContentType("application/json;charset=UTF-8");
        req.setContent("{\"msg\":\"badword\"}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, new MockFilterChain());

        assertThat(resp.getStatus()).isEqualTo(200);
        var root = new ObjectMapper().readTree(resp.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(io.github.genkidoudou.common.api.HttpCodes.SENSITIVE_WORD);
    }

    @Test
    void filter_skips_ignore_urls() throws Exception {
        SensitiveWordEngine engine = smallEngine();
        SensitiveWordFirewallProperties props = new SensitiveWordFirewallProperties();
        props.setIgnoreUrls(List.of("/pub/**"));
        props.setStrategy(SensitiveWordFirewallStrategy.THROW);
        SensitiveWordFirewallFilter filter = new SensitiveWordFirewallFilter(props, engine, new ObjectMapper());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/app/pub/x");
        req.setContextPath("/app");
        req.setServletPath("");
        req.setPathInfo("/pub/x");
        req.setMethod("POST");
        req.setContentType("application/json");
        req.setContent("{\"a\":\"badword\"}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse resp = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(req, resp, chain);
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isSameAs(req);
    }
}
