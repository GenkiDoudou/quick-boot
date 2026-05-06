package io.github.genkidoudou.common.security.firewall.methodandhost;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * {@code qc.security.firewall.method-and-host.enabled=true} 时注册请求方式与 Host 白名单拦截 Filter。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(MethodAndHostFirewallProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.method-and-host", name = "enabled", havingValue = "true")
public class MethodAndHostFirewallAutoConfiguration {

    @Bean
    public FilterRegistrationBean<MethodAndHostFirewallFilter> methodAndHostFirewallFilterRegistration(
            MethodAndHostFirewallProperties properties) {
        FilterRegistrationBean<MethodAndHostFirewallFilter> reg =
                new FilterRegistrationBean<>(new MethodAndHostFirewallFilter(properties));
        // 需晚于 CORS Filter：保证跨域时拦截响应也携带 CORS 响应头
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        reg.addUrlPatterns("/*");
        return reg;
    }
}

