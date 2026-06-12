package io.github.genkidoudou.web.workflow.handler;



import cn.hutool.core.util.StrUtil;

import cn.hutool.json.JSONUtil;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;

import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;

import io.github.genkidoudou.web.workflow.engine.NodeHandler;

import io.github.genkidoudou.web.workflow.engine.NodeResult;

import io.github.genkidoudou.web.workflow.engine.WorkflowContext;

import io.github.genkidoudou.web.workflow.util.JsonDeepParseUtil;

import org.springframework.stereotype.Component;



import java.util.HashMap;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;



/**

 * Start 节点：将运行入参写入节点输出，供后续节点通过 {@code {{start_x.key}}} 或 {@code {{inputs.key}}} 引用。

 * <p>

 * 按输入字段 {@code fieldType} 做类型转换（Object/Array 解析 JSON，并递归展开嵌套 JSON 字符串）。

 */

@Component

public class StartNodeHandler implements NodeHandler {



    @Override

    public String type() {

        return WfNodeType.START;

    }



    @Override

    @SuppressWarnings("unchecked")

    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {

        Map<String, Object> outputs = new HashMap<>(context.getRunInputs());

        Map<String, Object> data = node.getData();

        if (data != null && data.get("inputs") instanceof List<?> inputs) {

            for (Object item : inputs) {

                if (!(item instanceof Map<?, ?> inputDef)) {

                    continue;

                }

                Object keyObj = inputDef.get("key");

                if (keyObj == null) {

                    continue;

                }

                String key = String.valueOf(keyObj);

                Object raw = outputs.get(key);

                outputs.put(key, coerceInputValue(raw, (Map<String, Object>) inputDef));

            }

        }

        return NodeResult.success(outputs);

    }



    private Object coerceInputValue(Object raw, Map<String, Object> inputDef) {

        if (raw == null) {

            return null;

        }

        String fieldType = resolveFieldType(inputDef);

        return switch (fieldType) {

            case "object" -> coerceObject(raw);

            case "array" -> coerceArray(raw);

            default -> raw;

        };

    }



    private String resolveFieldType(Map<String, Object> inputDef) {

        Object fieldType = inputDef.get("fieldType");

        if (fieldType != null) {

            return migrateLegacyFieldType(String.valueOf(fieldType));

        }

        Object type = inputDef.get("type");

        if ("array".equals(type)) {

            return "array";

        }

        return "string";

    }



    private String migrateLegacyFieldType(String fieldType) {

        return switch (fieldType) {

            case "text", "paragraph", "select" -> "string";

            case "array[file]" -> "array";

            default -> fieldType;

        };

    }



    private Object coerceObject(Object raw) {

        Object parsed = parseJsonValue(raw);

        if (parsed instanceof Map<?, ?>) {

            return JsonDeepParseUtil.deepParse(parsed);

        }

        return parsed;

    }



    private Object coerceArray(Object raw) {

        Object parsed = parseJsonValue(raw);

        if (parsed instanceof List<?>) {

            return JsonDeepParseUtil.deepParse(parsed);

        }

        return parsed;

    }



    private Object parseJsonValue(Object raw) {

        if (raw instanceof Map<?, ?> || raw instanceof List<?>) {

            return raw;

        }

        if (raw instanceof String str) {

            if (StrUtil.isBlank(str)) {

                return new LinkedHashMap<>();

            }

            if (JSONUtil.isTypeJSON(str)) {

                if (JSONUtil.isTypeJSONArray(str)) {

                    return JSONUtil.parseArray(str);

                }

                return JSONUtil.parseObj(str);

            }

        }

        return raw;

    }

}


