package io.github.genkidoudou.common.security.firewall.xss;

import io.github.genkidoudou.common.security.firewall.sqlinjection.SqlInjectionFirewallAutoConfiguration;
import io.github.genkidoudou.common.security.firewall.sqlinjection.SqlInjectionFirewallFilter;
import io.github.genkidoudou.common.security.firewall.sensitiveword.SensitiveWordFirewallAutoConfiguration;
import io.github.genkidoudou.common.security.firewall.sensitiveword.SensitiveWordFirewallFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

class XssFirewallAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    XssFirewallAutoConfiguration.class,
                    SqlInjectionFirewallAutoConfiguration.class,
                    SensitiveWordFirewallAutoConfiguration.class
            ));

    @Test
    void disabled_noXssFilterBean() {
        runner.withPropertyValues(
                        "qc.security.firewall.xss.enabled=false",
                        "qc.security.firewall.sql-injection.enabled=false",
                        "qc.security.firewall.sensitive-word.enabled=false"
                )
                .run(ctx -> assertThat(ctx.getBeansOfType(XssFirewallFilter.class)).isEmpty());
    }

    @Test
    void enabled_ordersXssBeforeSqlBeforeSensitive() {
        runner.withPropertyValues(
                        "qc.security.firewall.xss.enabled=true",
                        "qc.security.firewall.sql-injection.enabled=true",
                        "qc.security.firewall.sensitive-word.enabled=true",
                        "qc.security.firewall.sensitive-word.strategy=THROW",
                        "qc.security.firewall.sensitive-word.black-list[0]=classpath:test-sensitive-black-sql.txt"
                )
                .run(ctx -> {
                    FilterRegistrationBean<?> xssReg =
                            ctx.getBean("xssFirewallFilterRegistration", FilterRegistrationBean.class);
                    FilterRegistrationBean<?> sqlReg =
                            ctx.getBean("sqlInjectionFirewallFilterRegistration", FilterRegistrationBean.class);
                    FilterRegistrationBean<?> sensReg =
                            ctx.getBean("sensitiveWordFirewallFilterRegistration", FilterRegistrationBean.class);
                    assertThat(xssReg.getFilter()).isInstanceOf(XssFirewallFilter.class);
                    assertThat(sqlReg.getFilter()).isInstanceOf(SqlInjectionFirewallFilter.class);
                    assertThat(sensReg.getFilter()).isInstanceOf(SensitiveWordFirewallFilter.class);
                    assertThat(xssReg.getOrder()).isLessThan(sqlReg.getOrder());
                    assertThat(sqlReg.getOrder()).isLessThan(sensReg.getOrder());
                });
    }
}
