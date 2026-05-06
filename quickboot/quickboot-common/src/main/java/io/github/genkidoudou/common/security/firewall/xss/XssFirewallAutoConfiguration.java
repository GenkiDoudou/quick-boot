package io.github.genkidoudou.common.security.firewall.xss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * {@code qc.security.firewall.xss.enabled=true} 时注册 XSS 启发式 Filter。
 * <p>
 * <b>顺序</b>：{@link Ordered#HIGHEST_PRECEDENCE} + 3；晚于 CORS（{@code HIGHEST_PRECEDENCE}），早于
 * SQL 注入（+4）、敏感词（+5）、Method/Host（+10）。
 * </p>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(XssFirewallProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.xss", name = "enabled", havingValue = "true")
public class XssFirewallAutoConfiguration {

    /**
     * 注册 XSS Filter；非法 {@code custom-patterns} 会在 {@link XssFirewallRuleSet} 构造时 fail-fast。
     */
    @Bean
    FilterRegistrationBean<XssFirewallFilter> xssFirewallFilterRegistration(
            XssFirewallProperties properties,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        XssFirewallRuleSet ruleSet = new XssFirewallRuleSet(properties);
        FilterRegistrationBean<XssFirewallFilter> reg =
                new FilterRegistrationBean<>(new XssFirewallFilter(properties, ruleSet, objectMapper));
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 3);
        reg.addUrlPatterns("/*");
        return reg;
    }
}
