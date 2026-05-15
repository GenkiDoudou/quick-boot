package io.github.genkidoudou.common.firewall.sqlinjection;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL关键字提供者
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
public class SqlKeywordsProvider {

    /**
     * 默认SQL关键字列表
     */
    private static final List<String> DEFAULT_KEYWORDS = List.of(
            // SQL 注入常见关键字
            "select", "insert", "update", "delete", "drop", "create", "alter",
            "exec", "execute", "script", "javascript", "union", "into", "load_file",
            "outfile", "dumpfile", "sub", "and", "or", "not", "use", "set",
            "concat", "join", "like", "order", "group", "having", "limit",
            "procedure", "handler", "call", "declare", "master", "truncate",
            "char", "chr", "mid", "substring", "substr", "load", "xp_cmdshell",
            "xp_regread", "sp_makewebtask", "xp_dirtree", "sp_executesql",
            // 特殊字符
            "\"", ";", "--", "/*", "*/", "xp_", "sp_", "0x"
    );

    private final SqlInjectionProperties properties;

    public SqlKeywordsProvider(SqlInjectionProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取SQL关键字列表
     *
     * @return SQL关键字列表
     * @since 2026/03/05
     */
    public List<String> getKeywords() {
        if (properties.getKeywords() != null && !properties.getKeywords().isEmpty()) {
            return properties.getKeywords();
        }
        return new ArrayList<>(DEFAULT_KEYWORDS);
    }
}
