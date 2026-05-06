package io.github.genkidoudou.common.security.firewall.headers;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 安全响应头防火墙自动配置：仅在 Servlet Web 应用且 {@code qc.security.firewall.headers.enabled=true} 时注册。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(FirewallHeadersProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.headers", name = "enabled", havingValue = "true")
public class FirewallSecurityHeadersAutoConfiguration {

    /**
     * 通过 {@link FilterRegistrationBean} 固定顺序，避免与链路上较早写出 JSON 的 Filter 行为冲突。
     */
    @Bean
    public FilterRegistrationBean<FirewallSecurityHeadersFilter> firewallSecurityHeadersFilterRegistration(
            FirewallHeadersProperties properties) {
        FilterRegistrationBean<FirewallSecurityHeadersFilter> registration =
                new FilterRegistrationBean<>(new FirewallSecurityHeadersFilter(properties));
        registration.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
