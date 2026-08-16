package io.github.genkidoudou.common.monitor.slowsql;

/**
 * 慢 SQL 语句操作类型（落库 {@code sql_type} 列取值）。
 */
public final class SlowSqlType {

    /** 查询语句。 */
    public static final String SELECT = "SELECT";
    /** 插入语句。 */
    public static final String INSERT = "INSERT";
    /** 更新语句。 */
    public static final String UPDATE = "UPDATE";
    /** 删除语句。 */
    public static final String DELETE = "DELETE";
    /** MERGE 语句。 */
    public static final String MERGE = "MERGE";
    /** 存储过程 / EXEC 语句。 */
    public static final String EXEC = "EXEC";
    /** CREATE 语句。 */
    public static final String CREATE = "CREATE";
    /** ALTER 语句。 */
    public static final String ALTER = "ALTER";
    /** DROP 语句。 */
    public static final String DROP = "DROP";
    /** TRUNCATE 语句。 */
    public static final String TRUNCATE = "TRUNCATE";
    /** 无法识别或未分类的语句类型。 */
    public static final String OTHER = "OTHER";

    private SlowSqlType() {
    }
}
