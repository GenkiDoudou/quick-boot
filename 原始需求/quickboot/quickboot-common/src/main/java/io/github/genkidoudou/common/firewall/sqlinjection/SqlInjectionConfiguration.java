package io.github.genkidoudou.common.firewall.sqlinjection;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * sql注入自动化配置类
 *
 * @author luyanan
 * @since 2026/3/7
 */
@ConditionalOnProperty(prefix = "qc.security.firewall.sql-injection", name = "enabled", havingValue = "true")
@Configuration
@EnableConfigurationProperties(SqlInjectionProperties.class)
public class SqlInjectionConfiguration {

    @Bean
    public SqlKeywordsProvider sqlKeywordsProvider(SqlInjectionProperties sqlInjectionProperties) {
        return new SqlKeywordsProvider(sqlInjectionProperties);
    }

    ;

    @Bean
    public SqlInjectionFilter sqlInjectionFilter(SqlInjectionProperties sqlInjectionProperties,
                                                 SqlKeywordsProvider sqlKeywordsProvider
    ) {
        return new SqlInjectionFilter(sqlInjectionProperties, sqlKeywordsProvider);
    }
}
