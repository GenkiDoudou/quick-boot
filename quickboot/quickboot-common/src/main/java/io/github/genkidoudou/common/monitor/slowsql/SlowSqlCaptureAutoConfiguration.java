package io.github.genkidoudou.common.monitor.slowsql;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * 慢 SQL 采集：事件发布支持类与 MyBatis mapper_id 标记拦截器（JDBC 落库在 web 模块 Druid Filter）。
 */
@AutoConfiguration
@EnableConfigurationProperties(SlowSqlProperties.class)
public class SlowSqlCaptureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "qc.monitor.slow-sql", name = "capture-enabled", havingValue = "true", matchIfMissing = true)
    public SlowSqlCaptureSupport slowSqlCaptureSupport(
        ApplicationEventPublisher eventPublisher,
        SlowSqlProperties properties
    ) {
        return new SlowSqlCaptureSupport(eventPublisher, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "qc.monitor.slow-sql", name = "capture-enabled", havingValue = "true", matchIfMissing = true)
    public SlowSqlMapperIdInnerInterceptor slowSqlMapperIdInnerInterceptor() {
        return new SlowSqlMapperIdInnerInterceptor();
    }
}
