package io.github.genkidoudou.report.internal.config;

import io.github.genkidoudou.report.internal.security.JimuShareAccessFilter;
import io.github.genkidoudou.report.internal.security.JimuTokenHeaderBridgeFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ConditionalOnProperty(prefix = "qc.jimu", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JimuProperties.class)
@ComponentScan("io.github.genkidoudou.report")
public class JimuReportAutoConfiguration {

    @Bean
    public JimuPrimaryDataSourceSynchronizer jimuPrimaryDataSourceSynchronizer(
        JdbcTemplate jdbcTemplate,
        DataSourceProperties dataSourceProperties,
        JimuProperties jimuProperties
    ) {
        return new JimuPrimaryDataSourceSynchronizer(jdbcTemplate, dataSourceProperties, jimuProperties);
    }

    @Bean
    public FilterRegistrationBean<JimuShareAccessFilter> jimuShareAccessFilterRegistration(JimuShareAccessFilter filter) {
        FilterRegistrationBean<JimuShareAccessFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(filter);
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<JimuTokenHeaderBridgeFilter> jimuTokenHeaderBridgeFilterRegistration() {
        FilterRegistrationBean<JimuTokenHeaderBridgeFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JimuTokenHeaderBridgeFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return bean;
    }
}
