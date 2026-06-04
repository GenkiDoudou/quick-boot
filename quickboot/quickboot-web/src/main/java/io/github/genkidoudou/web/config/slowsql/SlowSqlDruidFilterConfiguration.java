package io.github.genkidoudou.web.config.slowsql;

import com.alibaba.druid.filter.Filter;
import io.github.genkidoudou.common.monitor.slowsql.SlowSqlCaptureSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 {@link SlowSqlDruidFilter} 为 Spring {@link Filter} Bean。
 * <p>
 * Druid {@code DruidDataSourceWrapper#autoAddFilters} 会在 {@code init()} 前自动挂载，
 * 勿在 init 之后往 {@code proxyFilters} 追加（无效）。
 */
@Slf4j
@Configuration
@ConditionalOnClass(name = "com.alibaba.druid.pool.DruidDataSource")
@ConditionalOnProperty(prefix = "qc.monitor.slow-sql", name = "capture-enabled", havingValue = "true", matchIfMissing = true)
public class SlowSqlDruidFilterConfiguration {

    @Bean
    public Filter slowSqlDruidFilter(SlowSqlCaptureSupport captureSupport) {
        log.info("register SlowSqlDruidFilter (PreparedStatement + Statement), threshold via qc.monitor.slow-sql");
        return new SlowSqlDruidFilter(captureSupport);
    }
}
