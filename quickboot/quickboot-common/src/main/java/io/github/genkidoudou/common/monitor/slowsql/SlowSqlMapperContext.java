package io.github.genkidoudou.common.monitor.slowsql;

/**
 * 当前线程 MyBatis {@code MappedStatement#getId()}，供 Druid JDBC 采集器读取后清除。
 */
public final class SlowSqlMapperContext {

    private static final ThreadLocal<String> MAPPER_ID = new ThreadLocal<>();

    private SlowSqlMapperContext() {
    }

    /**
     * 设置当前线程 MyBatis mapper ID。
     *
     * @param mapperId {@code MappedStatement#getId()}；blank 时清除
     */
    public static void set(String mapperId) {
        if (mapperId == null || mapperId.isBlank()) {
            MAPPER_ID.remove();
        } else {
            MAPPER_ID.set(mapperId);
        }
    }

    /**
     * 读取并清除当前线程 mapper ID。
     *
     * @return mapper ID；未设置时返回空串
     */
    public static String getAndClear() {
        String v = MAPPER_ID.get();
        MAPPER_ID.remove();
        return v == null ? "" : v;
    }

    /** 清除当前线程 mapper ID。 */
    public static void clear() {
        MAPPER_ID.remove();
    }
}
