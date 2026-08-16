package io.github.genkidoudou.web.config.slowsql;

import com.alibaba.druid.filter.Filter;
import io.github.genkidoudou.common.monitor.slowsql.SlowSqlCaptureSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 {@link SlowSqlDruidFilter} 为 Spring {@link Filter} Bean。
 * <p>
 * Druid {@code DruidDataSourceWrapper#autoAddFilters} 会在 {@code init()} 前自动挂载，
 * 勿在 init 之后往 {@code proxyFilters} 追加（无效）。
 * <p>
 * 不使用 {@code @ConditionalOnBean(SlowSqlCaptureSupport)}：该类由 AutoConfiguration 提供，
 * 普通 {@code @Configuration} 上的 {@code @ConditionalOnBean} 可能过早判定失败导致 Filter 永不注册。
 * 改为 {@link ObjectProvider}，在 Bean 创建阶段再解析依赖（与 MybatisPlus 插件装配一致）。
 */
@Slf4j
@Configuration
@ConditionalOnClass(name = "com.alibaba.druid.pool.DruidDataSource")
@ConditionalOnProperty(prefix = "qc.monitor.slow-sql", name = "capture-enabled", havingValue = "true", matchIfMissing = true)
public class SlowSqlDruidFilterConfiguration {

    @Bean
    public Filter slowSqlDruidFilter(ObjectProvider<SlowSqlCaptureSupport> captureSupportProvider) {
        // getObject：创建时再取 AutoConfig 提供的 Support；缺失则启动失败（与 capture-enabled 一致）
        SlowSqlCaptureSupport captureSupport = captureSupportProvider.getObject();
        log.info("register SlowSqlDruidFilter (PreparedStatement + Statement), threshold via qc.monitor.slow-sql");
        return new SlowSqlDruidFilter(captureSupport);
    }
}
