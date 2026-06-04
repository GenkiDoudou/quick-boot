package io.github.genkidoudou.common.monitor.slowsql;

import cn.hutool.core.util.StrUtil;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从 SQL 文本解析操作类型（SELECT / INSERT / UPDATE / DELETE 等）。
 */
public final class SlowSqlTypeResolver {

    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern LINE_COMMENT = Pattern.compile("--[^\n\r]*");
    private static final Pattern FIRST_TOKEN = Pattern.compile("([A-Za-z_]+)");

    private SlowSqlTypeResolver() {
    }

    /**
     * @param sql 可执行或模板 SQL
     * @return {@link SlowSqlType} 常量字符串
     */
    public static String resolve(String sql) {
        if (StrUtil.isBlank(sql)) {
            return SlowSqlType.OTHER;
        }
        String stripped = stripComments(sql).trim();
        if (stripped.isEmpty()) {
            return SlowSqlType.OTHER;
        }
        String upper = stripped.toUpperCase(Locale.ROOT);
        if (upper.startsWith("WITH ")) {
            if (upper.contains(" SELECT") || upper.contains("\nSELECT")) {
                return SlowSqlType.SELECT;
            }
        }
        Matcher matcher = FIRST_TOKEN.matcher(upper);
        if (!matcher.find()) {
            return SlowSqlType.OTHER;
        }
        String first = matcher.group(1);
        return mapKeyword(first);
    }

    private static String mapKeyword(String keyword) {
        return switch (keyword) {
            case "SELECT" -> SlowSqlType.SELECT;
            case "INSERT", "REPLACE" -> SlowSqlType.INSERT;
            case "UPDATE" -> SlowSqlType.UPDATE;
            case "DELETE" -> SlowSqlType.DELETE;
            case "MERGE" -> SlowSqlType.MERGE;
            case "CALL", "EXEC", "EXECUTE" -> SlowSqlType.EXEC;
            case "CREATE" -> SlowSqlType.CREATE;
            case "ALTER" -> SlowSqlType.ALTER;
            case "DROP" -> SlowSqlType.DROP;
            case "TRUNCATE" -> SlowSqlType.TRUNCATE;
            default -> SlowSqlType.OTHER;
        };
    }

    private static String stripComments(String sql) {
        String s = BLOCK_COMMENT.matcher(sql).replaceAll(" ");
        return LINE_COMMENT.matcher(s).replaceAll(" ");
    }
}
