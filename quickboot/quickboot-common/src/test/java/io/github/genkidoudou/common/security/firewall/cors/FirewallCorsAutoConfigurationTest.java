package io.github.genkidoudou.common.security.firewall.cors;

import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.security.firewall.methodandhost.MethodAndHostFirewallAutoConfiguration;
import io.github.genkidoudou.common.security.firewall.methodandhost.MethodAndHostFirewallFilter;
import io.github.genkidoudou.common.security.firewall.methodandhost.MethodAndHostFirewallProperties;
import io.github.genkidoudou.common.security.firewall.sensitiveword.SensitiveWordFirewallAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.CorsFilter;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FirewallCorsAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    FirewallCorsAutoConfiguration.class,
                    SensitiveWordFirewallAutoConfiguration.class,
                    MethodAndHostFirewallAutoConfiguration.class
            ));

    @Test
    void disabled_hasNoCorsFilterRegistration() {
        runner.withPropertyValues("qc.security.firewall.cors.enabled=false")
                .run(context -> {
                    assertThat(context.getBeansOfType(FilterRegistrationBean.class).values())
                            .noneMatch(b -> b.getFilter() instanceof CorsFilter);
                });
    }

    @Test
    void simpleCors_requestOnMatchedPath_hasAllowOriginHeader() throws Exception {
        runner.withPropertyValues(
                        "qc.security.firewall.cors.enabled=true",
                        "qc.security.firewall.cors.path-pattern=/api/**",
                        "qc.security.firewall.cors.allowed-origins[0]=https://frontend.example.com"
                )
                .run(context -> {
                    FilterRegistrationBean<?> reg = context.getBean("firewallCorsFilterRegistration", FilterRegistrationBean.class);
                    CorsFilter filter = (CorsFilter) reg.getFilter();

                    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/user/profile");
                    req.addHeader("Origin", "https://frontend.example.com");
                    MockHttpServletResponse resp = new MockHttpServletResponse();

                    filter.doFilter(req, resp, new MockFilterChain());

                    assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("https://frontend.example.com");
                });
    }

    @Test
    void requestOnUnmatchedPath_hasNoCorsHeaders() throws Exception {
        runner.withPropertyValues(
                        "qc.security.firewall.cors.enabled=true",
                        "qc.security.firewall.cors.path-pattern=/api/**",
                        "qc.security.firewall.cors.allowed-origins[0]=https://frontend.example.com"
                )
                .run(context -> {
                    FilterRegistrationBean<?> reg = context.getBean("firewallCorsFilterRegistration", FilterRegistrationBean.class);
                    CorsFilter filter = (CorsFilter) reg.getFilter();

                    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/static/app.js");
                    req.addHeader("Origin", "https://frontend.example.com");
                    MockHttpServletResponse resp = new MockHttpServletResponse();

                    filter.doFilter(req, resp, new MockFilterChain());

                    assertThat(resp.getHeaderNames())
                            .noneMatch(h -> h.toLowerCase().startsWith("access-control-"));
                });
    }

    @Test
    void preflightOptions_returnsSuccessAndAllowHeaders() throws Exception {
        runner.withPropertyValues(
                        "qc.security.firewall.cors.enabled=true",
                        "qc.security.firewall.cors.path-pattern=/api/**",
                        "qc.security.firewall.cors.allowed-origins[0]=https://frontend.example.com"
                )
                .run(context -> {
                    FilterRegistrationBean<?> reg = context.getBean("firewallCorsFilterRegistration", FilterRegistrationBean.class);
                    CorsFilter filter = (CorsFilter) reg.getFilter();

                    MockHttpServletRequest req = new MockHttpServletRequest("OPTIONS", "/api/user/profile");
                    req.addHeader("Origin", "https://frontend.example.com");
                    req.addHeader("Access-Control-Request-Method", "POST");
                    MockHttpServletResponse resp = new MockHttpServletResponse();

                    filter.doFilter(req, resp, new MockFilterChain());

                    assertThat(resp.getStatus()).isBetween(200, 204);
                    assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("https://frontend.example.com");
                    assertThat(resp.getHeader("Access-Control-Allow-Methods")).contains("POST");
                });
    }

    @Test
    void wildcardOrigins_withCredentials_reflectsOriginAndVary() throws Exception {
        runner.withPropertyValues(
                        "qc.security.firewall.cors.enabled=true",
                        "qc.security.firewall.cors.path-pattern=/api/**",
                        "qc.security.firewall.cors.allowed-origins[0]=*",
                        "qc.security.firewall.cors.allow-credentials=true"
                )
                .run(context -> {
                    FilterRegistrationBean<?> reg = context.getBean("firewallCorsFilterRegistration", FilterRegistrationBean.class);
                    CorsFilter filter = (CorsFilter) reg.getFilter();

                    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/user/profile");
                    req.addHeader("Origin", "https://example.com");
                    MockHttpServletResponse resp = new MockHttpServletResponse();

                    filter.doFilter(req, resp, new MockFilterChain());

                    assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("https://example.com");
                    assertThat(resp.getHeaders("Vary")).anyMatch(v -> v.contains("Origin"));
                    assertThat(resp.getHeader("Access-Control-Allow-Credentials")).isEqualTo("true");
                });
    }

    @Test
    void corsOrder_isBeforeSensitiveWordAndMethodAndHost() {
        runner.withPropertyValues(
                        "qc.security.firewall.cors.enabled=true",
                        "qc.security.firewall.sensitive-word.enabled=true",
                        "qc.security.firewall.method-and-host.enabled=true"
                )
                .run(context -> {
                    FilterRegistrationBean<?> cors = context.getBean("firewallCorsFilterRegistration", FilterRegistrationBean.class);
                    FilterRegistrationBean<?> sensitive = context.getBean("sensitiveWordFirewallFilterRegistration", FilterRegistrationBean.class);
                    FilterRegistrationBean<?> methodAndHost = context.getBean("methodAndHostFirewallFilterRegistration", FilterRegistrationBean.class);

                    assertThat(cors.getOrder()).isLessThan(sensitive.getOrder());
                    assertThat(cors.getOrder()).isLessThan(methodAndHost.getOrder());
                });
    }

    @Test
    void blockedByMethodAndHost_stillHasCorsHeaders() throws Exception {
        // 以“CORS Filter 先执行”的方式模拟真实链路顺序，验证拦截响应也带 CORS 头。
        FirewallCorsProperties corsProps = new FirewallCorsProperties();
        corsProps.setEnabled(true);
        corsProps.setPathPattern("/**");
        corsProps.getAllowedOrigins().add("*");
        corsProps.setAllowCredentials(true);

        CorsFilter corsFilter = new CorsFilter(source(corsProps));

        MethodAndHostFirewallProperties mhProps = new MethodAndHostFirewallProperties();
        mhProps.setEnabled(true);
        mhProps.getAllowedMethods().add("GET");
        mhProps.getAllowedHosts().add("example.com:8080");
        mhProps.setForbiddenMessage("禁止访问");
        MethodAndHostFirewallFilter mhFilter = new MethodAndHostFirewallFilter(mhProps);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        req.addHeader("Origin", "https://example.com");
        req.addHeader("Host", "example.com:8080");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        corsFilter.doFilter(req, resp, (request, response) -> mhFilter.doFilter(request, response, new MockFilterChain()));

        assertThat(resp.getHeader("Access-Control-Allow-Origin")).isEqualTo("https://example.com");
        assertThat(resp.getContentAsString(StandardCharsets.UTF_8)).contains(String.valueOf(HttpCodes.METHOD_NOT_ALLOWED));
    }

    private static org.springframework.web.cors.CorsConfigurationSource source(FirewallCorsProperties properties) {
        var cors = FirewallCorsAutoConfiguration.buildCorsConfiguration(properties);
        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(properties.getPathPattern(), cors);
        return source;
    }
}

