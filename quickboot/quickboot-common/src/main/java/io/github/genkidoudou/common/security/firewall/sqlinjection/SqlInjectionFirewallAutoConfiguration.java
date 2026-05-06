package io.github.genkidoudou.common.security.firewall.sqlinjection;

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
 * {@code qc.security.firewall.sql-injection.enabled=true} 时注册 SQL 注入启发式 Filter。
 * <p>
 * <b>顺序</b>：{@link Ordered#HIGHEST_PRECEDENCE} + 4，晚于 XSS（+3）、CORS（{@code HIGHEST_PRECEDENCE}），早于敏感词（+5）、Method/Host（+10）。
 * 以便对<strong>原始</strong> Query/Form/body 做检测后再交由敏感词等 Filter 处理缓存后的流。
 * </p>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(SqlInjectionFirewallProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.sql-injection", name = "enabled", havingValue = "true")
public class SqlInjectionFirewallAutoConfiguration {

    /**
     * 注册 SQL 注入防火墙 Filter；order 与 CORS / 敏感词 / Method-Host 的相对关系见类 JavaDoc。
     */
    @Bean
    FilterRegistrationBean<SqlInjectionFirewallFilter> sqlInjectionFirewallFilterRegistration(
            SqlInjectionFirewallProperties properties,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        FilterRegistrationBean<SqlInjectionFirewallFilter> reg =
                new FilterRegistrationBean<>(new SqlInjectionFirewallFilter(properties, objectMapper));
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 4);
        reg.addUrlPatterns("/*");
        return reg;
    }
}
