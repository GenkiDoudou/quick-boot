package io.github.genkidoudou.common.monitor.slowsql;

/**
 * 慢 SQL 来源分类（落库 {@code sql_source} 字段取值）。
 */
public final class SlowSqlSource {

    /** 管理端 / 业务 MyBatis 等 HTTP 请求（非积木路径）。 */
    public static final String BUSINESS = "BUSINESS";

    /** 积木报表 / JimuBI 等（MiniDao / JDBC，URI 命中配置前缀）。 */
    public static final String JIMU = "JIMU";

    /** 无 HTTP 上下文：Flyway、启动任务等。 */
    public static final String SYSTEM = "SYSTEM";

    private SlowSqlSource() {
    }
}
