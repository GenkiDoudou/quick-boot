package io.github.genkidoudou.common.monitor.slowsql;

/**
 * 慢 SQL 语句操作类型（落库 {@code sql_type} 列取值）。
 */
public final class SlowSqlType {

    public static final String SELECT = "SELECT";
    public static final String INSERT = "INSERT";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String MERGE = "MERGE";
    public static final String EXEC = "EXEC";
    public static final String CREATE = "CREATE";
    public static final String ALTER = "ALTER";
    public static final String DROP = "DROP";
    public static final String TRUNCATE = "TRUNCATE";
    public static final String OTHER = "OTHER";

    private SlowSqlType() {
    }
}
