package io.github.genkidoudou.common.monitor.slowsql;

/**
 * 当前线程 MyBatis {@code MappedStatement#getId()}，供 Druid JDBC 采集器读取后清除。
 */
public final class SlowSqlMapperContext {

    private static final ThreadLocal<String> MAPPER_ID = new ThreadLocal<>();

    private SlowSqlMapperContext() {
    }

    public static void set(String mapperId) {
        if (mapperId == null || mapperId.isBlank()) {
            MAPPER_ID.remove();
        } else {
            MAPPER_ID.set(mapperId);
        }
    }

    public static String getAndClear() {
        String v = MAPPER_ID.get();
        MAPPER_ID.remove();
        return v == null ? "" : v;
    }

    public static void clear() {
        MAPPER_ID.remove();
    }
}
