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

/**
 * 积木报表 / JimuBI 集成自动配置：扫描 report 模块 Bean，注册数据源同步与鉴权 Filter。
 */
@Configuration
@ConditionalOnProperty(prefix = "qc.jimu", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JimuProperties.class)
@ComponentScan("io.github.genkidoudou.report")
public class JimuReportAutoConfiguration {

    /**
     * 启动后将积木内置数据源指向 QuickBoot 主库，避免演示库地址残留。
     */
    @Bean
    public JimuPrimaryDataSourceSynchronizer jimuPrimaryDataSourceSynchronizer(
        JdbcTemplate jdbcTemplate,
        DataSourceProperties dataSourceProperties,
        JimuProperties jimuProperties
    ) {
        return new JimuPrimaryDataSourceSynchronizer(jdbcTemplate, dataSourceProperties, jimuProperties);
    }

    /**
     * 分享/预览路径识别 Filter；副作用：在 request 上写入 {@code QC_JIMU_IS_PASS} 等属性。
     */
    @Bean
    public FilterRegistrationBean<JimuShareAccessFilter> jimuShareAccessFilterRegistration(JimuShareAccessFilter filter) {
        FilterRegistrationBean<JimuShareAccessFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(filter);
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return bean;
    }

    /**
     * Token Header 桥接 Filter；副作用：将 Authorization 透传为积木识别的 token / X-Access-Token。
     */
    @Bean
    public FilterRegistrationBean<JimuTokenHeaderBridgeFilter> jimuTokenHeaderBridgeFilterRegistration() {
        FilterRegistrationBean<JimuTokenHeaderBridgeFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JimuTokenHeaderBridgeFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return bean;
    }
}
