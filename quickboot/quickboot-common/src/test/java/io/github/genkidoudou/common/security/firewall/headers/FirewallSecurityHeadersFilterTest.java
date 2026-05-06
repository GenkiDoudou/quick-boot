package io.github.genkidoudou.common.security.firewall.headers;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FirewallSecurityHeadersFilter} 行为与 OpenSpec「firewall-security-headers」对齐的单测。
 */
class FirewallSecurityHeadersFilterTest {

    private final MockFilterChain chain = new MockFilterChain();

    @Test
    void normalPath_setsDefaultBasicHeaders_andNoStrictWhenUnset() throws ServletException, IOException {
        FirewallHeadersProperties props = propsEnabled();
        FirewallSecurityHeadersFilter filter = new FirewallSecurityHeadersFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_X_FRAME_OPTIONS)).isEqualTo("SAMEORIGIN");
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_X_CONTENT_TYPE_OPTIONS)).isEqualTo("nosniff");
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_X_XSS_PROTECTION)).isEqualTo("1; mode=block");
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_REFERRER_POLICY))
                .isEqualTo("strict-origin-when-cross-origin");
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_CSP)).isNull();
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_HSTS)).isNull();
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_PERMISSIONS_POLICY)).isNull();
    }

    @Test
    void normalPath_withStrictConfig_setsAll() throws ServletException, IOException {
        FirewallHeadersProperties props = propsEnabled();
        props.setContentSecurityPolicy("default-src 'none'");
        props.setStrictTransportSecurity("max-age=31536000");
        props.setPermissionsPolicy("geolocation=()");
        FirewallSecurityHeadersFilter filter = new FirewallSecurityHeadersFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/x");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_CSP)).isEqualTo("default-src 'none'");
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_HSTS)).isEqualTo("max-age=31536000");
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_PERMISSIONS_POLICY)).isEqualTo("geolocation=()");
    }

    @Test
    void fullExclude_setsNoManagedHeaders() throws ServletException, IOException {
        FirewallHeadersProperties props = propsEnabled();
        props.getExcludeUrls().add("/static/**");
        props.setContentSecurityPolicy("default-src 'self'");
        FirewallSecurityHeadersFilter filter = new FirewallSecurityHeadersFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/static/app.js");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_X_FRAME_OPTIONS)).isNull();
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_CSP)).isNull();
    }

    @Test
    void strictExclude_onlyBasic_notStrictEvenIfConfigured() throws ServletException, IOException {
        FirewallHeadersProperties props = propsEnabled();
        props.getExcludeFromStrictPolicyUrls().add("/swagger-ui/**");
        props.setContentSecurityPolicy("default-src 'none'");
        FirewallSecurityHeadersFilter filter = new FirewallSecurityHeadersFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_X_FRAME_OPTIONS)).isEqualTo("SAMEORIGIN");
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_CSP)).isNull();
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_HSTS)).isNull();
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_PERMISSIONS_POLICY)).isNull();
    }

    @Test
    void bothPatternsExcludeUrlWins_noHeaders() throws ServletException, IOException {
        FirewallHeadersProperties props = propsEnabled();
        props.getExcludeUrls().add("/pub/**");
        props.getExcludeFromStrictPolicyUrls().add("/pub/**");
        props.setContentSecurityPolicy("default-src 'none'");
        FirewallSecurityHeadersFilter filter = new FirewallSecurityHeadersFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/pub/callback");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_X_FRAME_OPTIONS)).isNull();
        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_CSP)).isNull();
    }

    @Test
    void contextPath_stillMatchesAntPattern() throws ServletException, IOException {
        FirewallHeadersProperties props = propsEnabled();
        props.getExcludeUrls().add("/api/**");
        FirewallSecurityHeadersFilter filter = new FirewallSecurityHeadersFilter(props);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ctx/api/foo");
        request.setContextPath("/ctx");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(FirewallSecurityHeadersFilter.HEADER_X_FRAME_OPTIONS)).isNull();
    }

    private static FirewallHeadersProperties propsEnabled() {
        FirewallHeadersProperties p = new FirewallHeadersProperties();
        p.setEnabled(true);
        return p;
    }
}
