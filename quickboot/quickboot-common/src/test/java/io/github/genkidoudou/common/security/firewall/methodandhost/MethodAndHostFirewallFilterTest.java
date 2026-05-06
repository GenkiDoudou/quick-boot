package io.github.genkidoudou.common.security.firewall.methodandhost;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.api.HttpCodes;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MethodAndHostFirewallFilter} 行为与 OpenSpec「firewall-method-and-host」对齐的单测。
 */
class MethodAndHostFirewallFilterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final MockFilterChain chain = new MockFilterChain();

    @Test
    void allowedMethods_blocksWhenNotWhitelisted() throws ServletException, IOException {
        MethodAndHostFirewallProperties props = enabledProps();
        props.getAllowedMethods().add("GET");
        props.setForbiddenMessage("禁止访问");
        MethodAndHostFirewallFilter filter = new MethodAndHostFirewallFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/x");
        request.addHeader("Host", "example.com:8080");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        JsonNode root = MAPPER.readTree(response.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.METHOD_NOT_ALLOWED);
        assertThat(root.get("msg").asText()).isEqualTo("禁止访问");
    }

    @Test
    void allowedHosts_blocksWhenHostMissing() throws ServletException, IOException {
        MethodAndHostFirewallProperties props = enabledProps();
        props.getAllowedHosts().add("example.com:8080");
        props.setForbiddenMessage("禁止访问");
        MethodAndHostFirewallFilter filter = new MethodAndHostFirewallFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/x");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        JsonNode root = MAPPER.readTree(response.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.HOST_NOT_ALLOWED);
        assertThat(root.get("msg").asText()).isEqualTo("禁止访问");
    }

    @Test
    void allowedHosts_matchesLowerCaseAndWildcardSubdomainAndPortWildcard() throws ServletException, IOException {
        MethodAndHostFirewallProperties props = enabledProps();
        props.getAllowedHosts().add("*.example.com:*");
        MethodAndHostFirewallFilter filter = new MethodAndHostFirewallFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/x");
        request.addHeader("Host", "Api.Example.Com:443");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getContentAsString()).isEmpty();
    }

    @Test
    void allowedHosts_wildcardDoesNotMatchRootDomain() throws ServletException, IOException {
        MethodAndHostFirewallProperties props = enabledProps();
        props.getAllowedHosts().add("*.example.com:*");
        props.setForbiddenMessage("禁止访问");
        MethodAndHostFirewallFilter filter = new MethodAndHostFirewallFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/x");
        request.addHeader("Host", "example.com:443");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        JsonNode root = MAPPER.readTree(response.getContentAsString(StandardCharsets.UTF_8));
        assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.HOST_NOT_ALLOWED);
    }

    @Test
    void allowedHosts_ipv6AndPortWildcard() throws ServletException, IOException {
        MethodAndHostFirewallProperties props = enabledProps();
        props.getAllowedHosts().add("[::1]:*");
        MethodAndHostFirewallFilter filter = new MethodAndHostFirewallFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/x");
        request.addHeader("Host", "[::1]:8080");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getContentAsString()).isEmpty();
    }

    @Test
    void excludeUrls_skipsAllChecks() throws ServletException, IOException {
        MethodAndHostFirewallProperties props = enabledProps();
        props.getExcludeUrls().add("/health/**");
        props.getAllowedMethods().add("GET");
        props.getAllowedHosts().add("example.com:8080");
        MethodAndHostFirewallFilter filter = new MethodAndHostFirewallFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/health/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getContentAsString()).isEmpty();
    }

    private static MethodAndHostFirewallProperties enabledProps() {
        MethodAndHostFirewallProperties p = new MethodAndHostFirewallProperties();
        p.setEnabled(true);
        return p;
    }
}

