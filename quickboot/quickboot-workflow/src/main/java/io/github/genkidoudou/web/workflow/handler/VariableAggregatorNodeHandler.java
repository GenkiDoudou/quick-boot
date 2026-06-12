package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Variable Aggregator 节点：合并多路变量到 aggregated 对象。
 */
@Component
public class VariableAggregatorNodeHandler implements NodeHandler {

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
        List<Map<String, Object>> variables = data.get("variables") instanceof List<?> list
            ? (List<Map<String, Object>>) list : List.of();
        Map<String, Object> aggregated = new HashMap<>();
        for (Map<String, Object> var : variables) {
            Object key = var.get("key");
            Object valueTemplate = var.get("value");
            if (key == null) {
                continue;
            }
            Object value = valueTemplate == null ? null
                : templateRenderer.resolveObject(String.valueOf(valueTemplate), context);
            if (value == null && valueTemplate != null) {
                value = templateRenderer.render(String.valueOf(valueTemplate), context);
            }
            aggregated.put(String.valueOf(key), value);
        }
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("aggregated", aggregated);
        return NodeResult.success(outputs);
    }
}
