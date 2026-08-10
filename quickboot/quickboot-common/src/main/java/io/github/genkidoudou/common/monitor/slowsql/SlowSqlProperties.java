package io.github.genkidoudou.common.monitor.slowsql;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 慢 SQL 采集配置（JDBC / Druid 层统一落库；MyBatis 仅补充 mapper_id）。
 */
@Data
@ConfigurationProperties(prefix = "qc.monitor.slow-sql")
public class SlowSqlProperties {

    /** 是否启用 JDBC 层慢 SQL 采集与落库。 */
    private boolean captureEnabled = true;

    /** 执行耗时超过该值（毫秒）记为慢 SQL。 */
    private long thresholdMs = 1000L;

    /** 是否在日志中输出慢 SQL（WARN）。 */
    private boolean logEnabled = true;

    /** 是否异步写入 {@code sys_slow_sql}。 */
    private boolean asyncEnabled = true;

    /** 落库 SQL 文本最大长度。 */
    private int maxSqlLength = 4000;

    /** 同步导出最大行数。 */
    private int exportMaxRows = 10_000;

    /**
     * 请求 URI 命中以下 Ant 前缀之一时 {@code sql_source=JIMU}（与 security 积木路径对齐）。
     */
    private List<String> jimuUriPrefixes = defaultJimuUriPrefixes();

    /**
     * SQL 文本包含以下子串时跳过采集（避免慢 SQL 表自写入递归等）。
     */
    private List<String> ignoreSqlContains = defaultIgnoreSqlContains();

    private static List<String> defaultJimuUriPrefixes() {
        List<String> p = new ArrayList<>();
        p.add("/jmreport/");
        p.add("/drag/");
        p.add("/jimubi/");
        p.add("/jimureport/");
        return p;
    }

    private static List<String> defaultIgnoreSqlContains() {
        List<String> p = new ArrayList<>();
        p.add("sys_slow_sql");
        return p;
    }
}
