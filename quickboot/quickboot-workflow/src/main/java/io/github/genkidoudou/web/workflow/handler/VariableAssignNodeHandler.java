package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.LoopExecutionScope;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Variable Assign 节点：将模板渲染结果写入多个 assignment 变量。
 */
@Component
public class VariableAssignNodeHandler implements NodeHandler {

    private final TemplateRenderer templateRenderer;

    public VariableAssignNodeHandler(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.VARIABLE_ASSIGN;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        List<Map<String, Object>> assignments = data.get("assignments") instanceof List<?> list
            ? (List<Map<String, Object>>) list : List.of();
        Map<String, Object> outputs = new HashMap<>();
        for (Map<String, Object> assignment : assignments) {
            Object keyObj = assignment.get("target");
            if (keyObj == null) {
                keyObj = assignment.get("key");
            }
            Object valueTemplate = assignment.get("value");
            if (keyObj == null) {
                continue;
            }
            String key = String.valueOf(keyObj).trim();
            String rendered = valueTemplate == null ? ""
                : templateRenderer.render(String.valueOf(valueTemplate), context);

            LoopExecutionScope scope = context.getCurrentLoopScope();
            if (scope != null && scope.getIntermediateKeys().contains(key)) {
                scope.getIntermediateVars().put(key, rendered);
                context.putNodeOutput(scope.getLoopNodeId(), key, rendered);
            }
            outputs.put(key, rendered);
        }
        return NodeResult.success(outputs);
    }
}
