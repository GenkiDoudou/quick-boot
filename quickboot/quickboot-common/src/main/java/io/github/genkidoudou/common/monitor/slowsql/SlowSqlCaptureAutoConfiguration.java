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

    /**
     * 慢 SQL 判定与事件发布支持类。
     *
     * @param eventPublisher 事件发布器
     * @param properties     慢 SQL 采集配置
     * @return 采集支持 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "qc.monitor.slow-sql", name = "capture-enabled", havingValue = "true", matchIfMissing = true)
    public SlowSqlCaptureSupport slowSqlCaptureSupport(
        ApplicationEventPublisher eventPublisher,
        SlowSqlProperties properties
    ) {
        return new SlowSqlCaptureSupport(eventPublisher, properties);
    }

    /**
     * MyBatis 内层拦截器：在 SQL 执行前将 {@code mapperId} 写入线程上下文。
     *
     * @return mapper_id 标记拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "qc.monitor.slow-sql", name = "capture-enabled", havingValue = "true", matchIfMissing = true)
    public SlowSqlMapperIdInnerInterceptor slowSqlMapperIdInnerInterceptor() {
        return new SlowSqlMapperIdInnerInterceptor();
    }
}
