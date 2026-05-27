package io.github.genkidoudou.report.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

/**
 * 官方演示数据把积木数据源指向 {@code 127.0.0.1:3306/jimureport}，与 QuickBoot 主库不一致；
 * 启动时按 {@link DataSourceProperties} 覆盖，使 JimuBI「SQL 解析」等走当前应用库。
 */
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.jimu", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JimuPrimaryDataSourceSynchronizer {

    private static final String UPDATE_BY_ID = """
            UPDATE jimu_report_data_source
            SET name = ?, code = ?, db_type = ?, db_driver = ?, db_url = ?,
                db_username = ?, db_password = ?, update_time = NOW(), connect_times = 0
            WHERE id = ?
            """;

    private static final String INSERT_PRIMARY = """
            INSERT INTO jimu_report_data_source
            (id, name, code, remark, db_type, db_driver, db_url, db_username, db_password,
             create_by, create_time, connect_times, tenant_id, type)
            VALUES (?, ?, ?, 'QuickBoot 应用主库（自动同步）', ?, ?, ?, ?, ?, 'system', NOW(), 0, '1', 'drag')
            """;

    private static final String FIX_DEMO_URLS = """
            UPDATE jimu_report_data_source
            SET name = ?, code = ?, db_type = ?, db_driver = ?, db_url = ?,
                db_username = ?, db_password = ?, update_time = NOW(), connect_times = 0
            WHERE db_url IS NOT NULL
              AND (db_url LIKE '%/jimureport%' OR db_url LIKE '%:3306/jimureport%'
                   OR db_url LIKE '%:3306/jimureport?%')
            """;

    private final JdbcTemplate jdbcTemplate;
    private final DataSourceProperties dataSourceProperties;
    private final JimuProperties jimuProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnReady() {
        JimuProperties.PrimaryDataSource cfg = jimuProperties.getPrimaryDataSource();
        if (!cfg.isSyncOnStartup()) {
            return;
        }
        String url = dataSourceProperties.determineUrl();
        if (!StringUtils.hasText(url)) {
            log.warn("跳过积木主库同步：spring.datasource.url 未配置");
            return;
        }
        JdbcBinding binding = JdbcBinding.fromUrl(url, dataSourceProperties);
        String password = dataSourceProperties.getPassword() != null ? dataSourceProperties.getPassword() : "";

        int fixed = jdbcTemplate.update(
                FIX_DEMO_URLS,
                cfg.getName(),
                cfg.getCode(),
                binding.dbType(),
                binding.driverClassName(),
                url,
                dataSourceProperties.determineUsername(),
                password);
        if (fixed > 0) {
            log.info("已修正 {} 条指向官方 jimureport 库的积木数据源 → {}", fixed, maskUrl(url));
        }

        int updated = jdbcTemplate.update(
                UPDATE_BY_ID,
                cfg.getName(),
                cfg.getCode(),
                binding.dbType(),
                binding.driverClassName(),
                url,
                dataSourceProperties.determineUsername(),
                password,
                cfg.getId());
        if (updated == 0) {
            jdbcTemplate.update(
                    INSERT_PRIMARY,
                    cfg.getId(),
                    cfg.getName(),
                    cfg.getCode(),
                    binding.dbType(),
                    binding.driverClassName(),
                    url,
                    dataSourceProperties.determineUsername(),
                    password);
            log.info("已创建积木主库数据源 id={}, url={}", cfg.getId(), maskUrl(url));
        } else {
            log.info("已同步积木主库数据源 id={}, url={}", cfg.getId(), maskUrl(url));
        }
    }

    private static String maskUrl(String url) {
        if (url == null || url.length() < 32) {
            return url;
        }
        return url.substring(0, 28) + "...";
    }

    private record JdbcBinding(String dbType, String driverClassName) {

        static JdbcBinding fromUrl(String url, DataSourceProperties props) {
            if (StringUtils.hasText(props.getDriverClassName())) {
                return new JdbcBinding(guessDbType(url), props.getDriverClassName());
            }
            String lower = url.toLowerCase();
            if (lower.startsWith("jdbc:mariadb:")) {
                return new JdbcBinding("MYSQL5.7", "org.mariadb.jdbc.Driver");
            }
            if (lower.startsWith("jdbc:mysql:")) {
                return new JdbcBinding("MYSQL5.7", "com.mysql.cj.jdbc.Driver");
            }
            return new JdbcBinding("MYSQL5.7", "com.mysql.cj.jdbc.Driver");
        }

        private static String guessDbType(String url) {
            return "MYSQL5.7";
        }
    }
}
