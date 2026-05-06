package io.github.genkidoudou.common.security.firewall.sqlinjection;

import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.security.firewall.sensitiveword.SensitiveWordFirewallAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQL 注入防火墙自动配置：开关、相对 Order。
 */
class SqlInjectionFirewallAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SqlInjectionFirewallAutoConfiguration.class,
                    SensitiveWordFirewallAutoConfiguration.class
            ));

    @Test
    void disabled_doesNotRegisterSqlInjectionFilter() {
        runner.withPropertyValues(
                        "qc.security.firewall.sql-injection.enabled=false",
                        "qc.security.firewall.sensitive-word.enabled=false"
                )
                .run(context -> assertThat(context.getBeansOfType(SqlInjectionFirewallFilter.class)).isEmpty());
    }

    @Test
    void enabled_registersFilterBeforeSensitiveWord() {
        runner.withPropertyValues(
                        "qc.security.firewall.sql-injection.enabled=true",
                        "qc.security.firewall.sensitive-word.enabled=true",
                        "qc.security.firewall.sensitive-word.strategy=THROW",
                        "qc.security.firewall.sensitive-word.black-list[0]=classpath:test-sensitive-black-sql.txt"
                )
                .run(context -> {
                    FilterRegistrationBean<?> sqlReg =
                            context.getBean("sqlInjectionFirewallFilterRegistration", FilterRegistrationBean.class);
                    FilterRegistrationBean<?> sensReg =
                            context.getBean("sensitiveWordFirewallFilterRegistration", FilterRegistrationBean.class);
                    assertThat(sqlReg.getFilter()).isInstanceOf(SqlInjectionFirewallFilter.class);
                    assertThat(sqlReg.getOrder()).isLessThan(sensReg.getOrder());
                });
    }
}
