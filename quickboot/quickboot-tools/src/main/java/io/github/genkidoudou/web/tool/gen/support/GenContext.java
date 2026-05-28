package io.github.genkidoudou.web.tool.gen.support;

import io.github.genkidoudou.web.tool.gen.domain.GenTable;
import io.github.genkidoudou.web.tool.gen.domain.GenTableColumn;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FreeMarker 渲染上下文。
 */
@Getter
public class GenContext {

    private final Map<String, Object> model = new HashMap<>();

    public GenContext(GenTable table, List<GenTableColumn> columns) {
        String className = table.getClassName();
        String classLower = className == null ? "" : Character.toLowerCase(className.charAt(0)) + className.substring(1);
        String module = table.getModuleName();
        String business = table.getBusinessName();
        String perm = module + ":" + business;

        model.put("table", table);
        model.put("columns", columns);
        model.put("className", className);
        model.put("classLower", classLower);
        model.put("tableName", table.getTableName());
        model.put("tableComment", table.getTableComment());
        model.put("packageName", table.getPackageName());
        model.put("moduleName", module);
        model.put("businessName", business);
        model.put("functionName", table.getFunctionName());
        model.put("author", table.getFunctionAuthor());
        model.put("permissionPrefix", perm);
        model.put("parentMenuId", table.getParentMenuId());

        GenTableColumn pk = columns.stream().filter(c -> "1".equals(c.getIsPk())).findFirst().orElse(null);
        model.put("pkColumn", pk);
        if (pk != null) {
            model.put("pkField", pk.getJavaField());
            model.put("pkJavaType", pk.getJavaType());
            model.put("pkColumnName", pk.getColumnName());
        }

        model.put("queryColumns", columns.stream().filter(c -> "1".equals(c.getIsQuery())).collect(Collectors.toList()));
        model.put("listColumns", columns.stream().filter(c -> "1".equals(c.getIsList())).collect(Collectors.toList()));
        model.put("editColumns", columns.stream().filter(c -> "1".equals(c.getIsEdit())).collect(Collectors.toList()));
    }
}
