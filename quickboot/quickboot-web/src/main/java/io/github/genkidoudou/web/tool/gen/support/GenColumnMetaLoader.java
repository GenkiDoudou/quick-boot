package io.github.genkidoudou.web.tool.gen.support;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从 information_schema 读取列注释与完整类型（弥补 MP Generator 未带出注释的情况）。
 */
@Component
@RequiredArgsConstructor
public class GenColumnMetaLoader {

    private final JdbcTemplate jdbcTemplate;

    /**
     * @param tableName 物理表名
     * @return key 为列名（小写）
     */
    public Map<String, ColumnMeta> loadByTable(String tableName) {
        String sql = """
            SELECT COLUMN_NAME, COLUMN_COMMENT, COLUMN_TYPE
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = ?
            ORDER BY ORDINAL_POSITION
            """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tableName);
        Map<String, ColumnMeta> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String name = String.valueOf(row.get("COLUMN_NAME"));
            String comment = row.get("COLUMN_COMMENT") != null ? String.valueOf(row.get("COLUMN_COMMENT")) : "";
            String columnType = row.get("COLUMN_TYPE") != null ? String.valueOf(row.get("COLUMN_TYPE")) : "";
            map.put(name.toLowerCase(), new ColumnMeta(name, StrUtil.trim(comment), StrUtil.trim(columnType)));
        }
        return map;
    }

    /**
     * 列元数据。
     *
     * @param columnName   列名
     * @param columnComment 列注释
     * @param columnType   完整类型，如 varchar(64)
     */
    public record ColumnMeta(String columnName, String columnComment, String columnType) {
    }
}
