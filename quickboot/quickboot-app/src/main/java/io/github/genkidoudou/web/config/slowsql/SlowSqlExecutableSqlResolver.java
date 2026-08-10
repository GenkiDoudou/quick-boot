package io.github.genkidoudou.web.config.slowsql;

import com.alibaba.druid.DbType;
import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.SQLUtils;
import cn.hutool.core.util.StrUtil;

import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.temporal.Temporal;
import java.util.Locale;

/**
 * 将 Druid 拦截到的 PreparedStatement 转为可读的完整 SQL（参数代入 + 格式化）。
 */
public final class SlowSqlExecutableSqlResolver {

    private static final DbType DEFAULT_DB_TYPE = DbType.mysql;

    private SlowSqlExecutableSqlResolver() {
    }

    /**
     * @param statement JDBC 代理（PreparedStatement 时合并参数）
     * @param rawSql    执行前传入的 SQL 模板，可为空
     * @return 可执行、已格式化的 SQL 文本
     */
    public static String resolve(StatementProxy statement, String rawSql) {
        String sql = StrUtil.isNotBlank(rawSql) ? rawSql.trim() : "";
        if (sql.isEmpty() && statement != null) {
            sql = StrUtil.blankToDefault(statement.getLastExecuteSql(), "").trim();
        }
        if (statement instanceof PreparedStatementProxy pstmt) {
            sql = mergeParameters(sql, pstmt);
        }
        return formatSql(sql);
    }

    private static String mergeParameters(String sql, PreparedStatementProxy pstmt) {
        if (StrUtil.isBlank(sql) || !sql.contains("?")) {
            return sql;
        }
        int size = pstmt.getParametersSize();
        if (size <= 0) {
            return sql;
        }
        StringBuilder out = new StringBuilder(sql.length() + size * 12);
        int paramIdx = 0;
        boolean inSingle = false;
        boolean inDouble = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
                out.append(c);
            } else if (c == '"' && !inSingle) {
                inDouble = !inDouble;
                out.append(c);
            } else if (c == '?' && !inSingle && !inDouble && paramIdx < size) {
                out.append(formatParameterLiteral(readParameterValue(pstmt, paramIdx++)));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static Object readParameterValue(PreparedStatementProxy pstmt, int index) {
        try {
            JdbcParameter parameter = pstmt.getParameter(index);
            return parameter == null ? null : parameter.getValue();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String formatParameterLiteral(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Boolean b) {
            return b ? "1" : "0";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof byte[] bytes) {
            return "0x" + bytesToHex(bytes);
        }
        if (value instanceof Temporal || value instanceof Date || value instanceof Time || value instanceof Timestamp) {
            return quote(String.valueOf(value));
        }
        if (value instanceof Clob || value instanceof NClob) {
            return quote("[CLOB]");
        }
        if (value instanceof Enum<?> e) {
            return quote(e.name());
        }
        return quote(String.valueOf(value));
    }

    private static String quote(String s) {
        if (s == null) {
            return "NULL";
        }
        return "'" + s.replace("'", "''") + "'";
    }

    private static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.ROOT, "%02X", b));
        }
        return sb.toString();
    }

    private static String formatSql(String sql) {
        if (StrUtil.isBlank(sql)) {
            return "";
        }
        try {
            return SQLUtils.format(sql, DEFAULT_DB_TYPE);
        } catch (Exception ignored) {
            return sql;
        }
    }
}
