package io.github.genkidoudou.web.knowledge.mcp.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.dto.McpToolInfoVo;
import io.github.genkidoudou.web.knowledge.dto.McpToolParamVo;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 将 MCP SDK {@link McpSchema.Tool} 转为前端可展示的 VO（含入参 schema）。
 */
public final class McpToolInfoSupport {

    private McpToolInfoSupport() {
    }

    /**
     * @param tool MCP SDK 工具定义
     * @return 含参数列表与完整 schema 的 VO
     */
    public static McpToolInfoVo toVo(McpSchema.Tool tool) {
        McpToolInfoVo vo = new McpToolInfoVo();
        vo.setName(tool.name());
        vo.setTitle(tool.title());
        vo.setDescription(tool.description());
        if (tool.inputSchema() != null) {
            vo.setInputSchema(toSchemaMap(tool.inputSchema()));
            vo.setParameters(parseParameters(tool.inputSchema()));
        }
        if (tool.outputSchema() != null && !tool.outputSchema().isEmpty()) {
            vo.setOutputSchema(tool.outputSchema());
        }
        return vo;
    }

    private static Map<String, Object> toSchemaMap(McpSchema.JsonSchema schema) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (StrUtil.isNotBlank(schema.type())) {
            map.put("type", schema.type());
        }
        if (schema.properties() != null && !schema.properties().isEmpty()) {
            map.put("properties", schema.properties());
        }
        if (schema.required() != null && !schema.required().isEmpty()) {
            map.put("required", schema.required());
        }
        if (schema.additionalProperties() != null) {
            map.put("additionalProperties", schema.additionalProperties());
        }
        if (schema.defs() != null && !schema.defs().isEmpty()) {
            map.put("$defs", schema.defs());
        }
        if (schema.definitions() != null && !schema.definitions().isEmpty()) {
            map.put("definitions", schema.definitions());
        }
        return map;
    }

    private static List<McpToolParamVo> parseParameters(McpSchema.JsonSchema schema) {
        Map<String, Object> properties = schema.properties();
        if (properties == null || properties.isEmpty()) {
            return List.of();
        }
        Set<String> requiredSet = schema.required() == null
            ? Set.of()
            : new HashSet<>(schema.required());
        List<McpToolParamVo> params = new ArrayList<>(properties.size());
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            McpToolParamVo param = new McpToolParamVo();
            param.setName(entry.getKey());
            param.setRequired(requiredSet.contains(entry.getKey()));
            if (entry.getValue() instanceof Map<?, ?> prop) {
                param.setType(resolveType(prop));
                param.setDescription(stringOrNull(prop.get("description")));
                Object def = prop.get("default");
                if (def != null) {
                    param.setDefaultValue(String.valueOf(def));
                }
                Object enumVal = prop.get("enum");
                if (enumVal instanceof List<?> list) {
                    List<String> enums = new ArrayList<>(list.size());
                    for (Object item : list) {
                        if (item != null) {
                            enums.add(String.valueOf(item));
                        }
                    }
                    param.setEnumValues(enums);
                }
            }
            params.add(param);
        }
        return params;
    }

    @SuppressWarnings("unchecked")
    private static String resolveType(Map<?, ?> prop) {
        Object type = prop.get("type");
        if (type != null) {
            return String.valueOf(type);
        }
        Object anyOf = prop.get("anyOf");
        if (anyOf instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> first) {
            Object t = first.get("type");
            if (t != null) {
                return String.valueOf(t);
            }
        }
        return "object";
    }

    private static String stringOrNull(Object value) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value);
        return StrUtil.isBlank(s) ? null : s;
    }
}
