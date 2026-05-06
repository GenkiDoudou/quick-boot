package io.github.genkidoudou.common.security.firewall.sqlinjection;

import java.util.List;

/**
 * 内置 SQL 注入启发式关键字（统一小写），当配置 {@code keywords} 为空时使用。
 * <p>
 * 采用<strong>子串、大小写不敏感</strong>匹配，策略偏<strong>宁可拦宽</strong>，可能误杀合法文本；
 * 不属于语法级 SQL 解析，仅作 Servlet 入口软拦截。
 * </p>
 */
final class SqlInjectionFirewallKeywordDefaults {

    /**
     * 默认关键字列表（已小写）；与配置 {@code keywords} 非空时的语义一致（配置项也会规范为小写参与匹配）。
     */
    static final List<String> DEFAULT_KEYWORDS = List.of(
            "select",
            "union",
            "insert",
            "delete",
            "update",
            "drop",
            "truncate",
            "exec",
            "execute",
            "script",
            "--",
            "/*",
            "*/",
            "1=1",
            "or 1=1",
            "and 1=1",
            "sleep(",
            "benchmark(",
            "information_schema",
            "sysobjects",
            "xp_cmdshell",
            "0x",
            "char(",
            "concat(",
            "@@",
            " into ",
            " outfile",
            "load_file",
            "having",
            " order by ",
            " group by "
    );

    private SqlInjectionFirewallKeywordDefaults() {
    }
}
