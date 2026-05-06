package io.github.genkidoudou.common.security.firewall.sensitiveword;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;

/**
 * {@code qc.security.firewall.sensitive-word.enabled=true} 时注册敏感词 Filter 与引擎。
 * <p>
 * <b>顺序</b>：{@link Ordered#HIGHEST_PRECEDENCE} + 5；晚于 XSS（+3）、SQL 注入（+4），且需晚于 CORS Filter。
 * </p>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(SensitiveWordFirewallProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.sensitive-word", name = "enabled", havingValue = "true")
public class SensitiveWordFirewallAutoConfiguration {

    @Bean
    SensitiveWordEngine sensitiveWordEngine(SensitiveWordFirewallProperties properties,
                                           ResourceLoader resourceLoader) {
        return SensitiveWordEngine.create(properties, resourceLoader);
    }

    @Bean
    FilterRegistrationBean<SensitiveWordFirewallFilter> sensitiveWordFirewallFilterRegistration(
            SensitiveWordFirewallProperties properties,
            SensitiveWordEngine engine,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        FilterRegistrationBean<SensitiveWordFirewallFilter> reg = new FilterRegistrationBean<>(
                new SensitiveWordFirewallFilter(properties, engine, objectMapper));
        // 需晚于 CORS：保证跨域时拦截响应也携带 CORS 响应头；晚于 XSS/SQL 以对缓存后的原始 body 做敏感词处理
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        reg.addUrlPatterns("/*");
        return reg;
    }
}
