package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 变量聚合节点：按分组将多路分支变量聚合为 Group1、Group2…；
 * 策略为取组内第一个非空值，避免未执行分支导致下游报错。
 */
@Component
public class VariableAggregatorNodeHandler implements NodeHandler {

    private static final String STRATEGY_FIRST_NON_EMPTY = "first_non_empty";

    private final TemplateRenderer templateRenderer;

    public VariableAggregatorNodeHandler(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.VARIABLE_AGGREGATOR;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        List<Map<String, Object>> groups = resolveGroups(data);
        Map<String, Object> outputs = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
            Map<String, Object> group = groups.get(i);
            String outputKey = resolveGroupName(group, i + 1);
            Object value = aggregateGroup(group, context);
            if (value != null) {
                outputs.put(outputKey, value);
            }
        }
        return NodeResult.success(outputs);
    }

    /**
     * 解析分组配置，兼容旧版根级 variables 列表。
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> resolveGroups(Map<String, Object> data) {
        if (data.get("groups") instanceof List<?> groupList && !groupList.isEmpty()) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : groupList) {
                if (item instanceof Map<?, ?> map) {
                    result.add((Map<String, Object>) map);
                }
            }
            return result;
        }

        List<String> legacyTemplates = extractLegacyVariableTemplates(data.get("variables"));
        if (legacyTemplates.isEmpty()) {
            return List.of();
        }
        Map<String, Object> legacyGroup = new HashMap<>();
        legacyGroup.put("id", "group_1");
        legacyGroup.put("name", "Group1");
        legacyGroup.put("strategy", STRATEGY_FIRST_NON_EMPTY);
        legacyGroup.put("variables", legacyTemplates);
        return List.of(legacyGroup);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractLegacyVariableTemplates(Object variablesObj) {
        if (!(variablesObj instanceof List<?> list)) {
            return List.of();
        }
        List<String> templates = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof String str) {
                if (StrUtil.isNotBlank(str)) {
                    templates.add(str.trim());
                }
                continue;
            }
            if (item instanceof Map<?, ?> map) {
                Object value = map.get("value");
                if (value != null && StrUtil.isNotBlank(String.valueOf(value))) {
                    templates.add(String.valueOf(value).trim());
                }
            }
        }
        return templates;
    }

    private String resolveGroupName(Map<String, Object> group, int index) {
        Object name = group.get("name");
        if (name != null && StrUtil.isNotBlank(String.valueOf(name))) {
            return String.valueOf(name).trim();
        }
        return "Group" + index;
    }

    @SuppressWarnings("unchecked")
    private Object aggregateGroup(Map<String, Object> group, WorkflowContext context) {
        String strategy = group.get("strategy") == null
            ? STRATEGY_FIRST_NON_EMPTY
            : String.valueOf(group.get("strategy"));
        if (!STRATEGY_FIRST_NON_EMPTY.equals(strategy)) {
            strategy = STRATEGY_FIRST_NON_EMPTY;
        }
        List<String> templates = extractLegacyVariableTemplates(group.get("variables"));
        for (String template : templates) {
            Object value = resolveTemplateValue(template, context);
            if (isNonEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    private Object resolveTemplateValue(String template, WorkflowContext context) {
        if (StrUtil.isBlank(template)) {
            return null;
        }
        String expr = template.trim();
        if (expr.startsWith("{{") && expr.endsWith("}}")) {
            Object resolved = templateRenderer.resolveObject(expr.substring(2, expr.length() - 2).trim(), context);
            if (resolved != null) {
                return resolved;
            }
        }
        Object resolved = templateRenderer.resolveObject(expr, context);
        if (resolved != null) {
            return resolved;
        }
        String rendered = templateRenderer.render(expr, context);
        return StrUtil.isBlank(rendered) ? null : rendered;
    }

    private boolean isNonEmpty(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String str) {
            return StrUtil.isNotBlank(str);
        }
        if (value instanceof Collection<?> collection) {
            return !collection.isEmpty();
        }
        if (value instanceof Map<?, ?> map) {
            return !map.isEmpty();
        }
        return true;
    }
}
