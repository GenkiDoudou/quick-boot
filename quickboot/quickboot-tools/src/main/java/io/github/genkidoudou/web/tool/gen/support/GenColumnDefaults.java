package io.github.genkidoudou.web.tool.gen.support;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import io.github.genkidoudou.web.tool.gen.domain.GenTable;
import io.github.genkidoudou.web.tool.gen.domain.GenTableColumn;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 将 MP {@link TableInfo} 转为 gen_table / gen_table_column 默认值。
 */
@Component
public class GenColumnDefaults {

    private final GenConfigResolver genConfigResolver;
    private final GenColumnMetaLoader columnMetaLoader;

    public GenColumnDefaults(GenConfigResolver genConfigResolver, GenColumnMetaLoader columnMetaLoader) {
        this.genConfigResolver = genConfigResolver;
        this.columnMetaLoader = columnMetaLoader;
    }

    /**
     * 由库表信息初始化 gen_table 行（未持久化）。
     */
    public GenTable toGenTable(TableInfo tableInfo) {
        GenTable table = new GenTable();
        table.setTableName(tableInfo.getName());
        table.setTableComment(StrUtil.blankToDefault(tableInfo.getComment(), tableInfo.getName()));
        table.setClassName(tableInfo.getEntityName());
        table.setTplCategory("crud");
        table.setTplWebType("c7");
        table.setPackageName(genConfigResolver.getPackageName());
        table.setModuleName(genConfigResolver.getModuleName());
        table.setTplCategory(genConfigResolver.getTplCategory());
        table.setParentMenuId(genConfigResolver.getParentMenuId());
        String business = StrUtil.toCamelCase(StrUtil.removePrefix(tableInfo.getName(), "sys_"));
        table.setBusinessName(business);
        table.setFunctionName(table.getTableComment());
        table.setFunctionAuthor(genConfigResolver.getAuthor());
        table.setGenType("0");
        return table;
    }

    /**
     * 由库表字段初始化列配置（未持久化）。
     */
    public List<GenTableColumn> toGenColumns(TableInfo tableInfo, Long tableId) {
        Map<String, GenColumnMetaLoader.ColumnMeta> metaMap =
            columnMetaLoader.loadByTable(tableInfo.getName());
        List<GenTableColumn> columns = new ArrayList<>();
        int sort = 1;
        for (TableField field : tableInfo.getFields()) {
            columns.add(toGenColumn(field, tableId, sort++, metaMap.get(field.getName().toLowerCase())));
        }
        return columns;
    }

    private GenTableColumn toGenColumn(TableField field, Long tableId, int sort, GenColumnMetaLoader.ColumnMeta meta) {
        GenTableColumn col = new GenTableColumn();
        col.setTableId(tableId);
        col.setColumnName(field.getName());
        String mpType = field.getColumnType() != null ? field.getColumnType().getType() : "";
        if (meta != null && StrUtil.isNotBlank(meta.columnType())) {
            col.setColumnType(meta.columnType());
        } else {
            col.setColumnType(mpType);
        }
        String comment = StrUtil.trim(field.getComment());
        if (meta != null && StrUtil.isNotBlank(meta.columnComment())) {
            comment = meta.columnComment();
        }
        if (StrUtil.isBlank(comment) || comment.equalsIgnoreCase(field.getName())) {
            comment = StrUtil.blankToDefault(comment, field.getName());
        }
        col.setColumnComment(comment);
        col.setJavaType(field.getPropertyType());
        col.setJavaField(field.getPropertyName());
        col.setIsPk(field.isKeyFlag() ? "1" : "0");
        col.setIsIncrement(field.isKeyFlag() && field.isKeyIdentityFlag() ? "1" : "0");
        col.setIsRequired("0");
        col.setIsInsert("1");
        col.setSort(sort);
        col.setQueryType("EQ");

        String name = field.getName();
        if ("1".equals(col.getIsPk())) {
            col.setIsList("0");
            col.setIsQuery("0");
            col.setIsEdit("0");
            col.setIsRequired("0");
        } else if ("remark".equals(name)) {
            col.setIsList("0");
            col.setIsQuery("0");
            col.setIsEdit("1");
        } else if ("create_by".equals(name) || "update_time".equals(name) || "update_by".equals(name)) {
            col.setIsList("0");
            col.setIsQuery("0");
            col.setIsEdit("0");
        } else if ("del_flag".equals(name)) {
            col.setIsList("0");
            col.setIsEdit("0");
            col.setIsQuery("0");
        } else if ("create_time".equals(name)) {
            col.setIsList("1");
            col.setIsEdit("0");
            col.setIsQuery("0");
        } else {
            col.setIsList("1");
            col.setIsEdit("1");
            col.setIsQuery("1");
        }

        if ("String".equals(col.getJavaType())) {
            col.setQueryType("LIKE");
            String dictType = ReUtil.get("\\[(.*?)]", col.getColumnComment(), 1);
            if (StrUtil.isNotBlank(dictType)) {
                col.setHtmlType("select");
                col.setDictType(dictType);
            } else {
                col.setHtmlType("input");
            }
        } else if ("LocalDateTime".equals(col.getJavaType()) || "Date".equals(col.getJavaType())) {
            col.setHtmlType("datetime");
            col.setQueryType("BETWEEN");
        } else {
            col.setHtmlType("input");
            if (!"String".equals(col.getJavaType())) {
                col.setQueryType("EQ");
            }
        }
        return col;
    }
}
