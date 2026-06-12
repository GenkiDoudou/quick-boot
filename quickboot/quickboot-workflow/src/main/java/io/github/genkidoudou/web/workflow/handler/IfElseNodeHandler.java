package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.constants.WorkflowConstants;
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
 * If-Else 节点：根据条件组评估 true/false 分支。
 */
@Component
public class IfElseNodeHandler implements NodeHandler {

    private final TemplateRenderer templateRenderer;

    public IfElseNodeHandler(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.IF_ELSE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        List<Map<String, Object>> conditions = data.get("conditions") instanceof List<?> list
            ? (List<Map<String, Object>>) list : List.of();
        String logic = data.get("logic") == null ? "AND" : String.valueOf(data.get("logic"));
        boolean result = evaluateConditions(conditions, logic, context);
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("branch", result ? WorkflowConstants.HANDLE_TRUE : WorkflowConstants.HANDLE_FALSE);
        return NodeResult.successWithBranch(outputs, result ? WorkflowConstants.HANDLE_TRUE : WorkflowConstants.HANDLE_FALSE);
    }

    private boolean evaluateConditions(List<Map<String, Object>> conditions, String logic, WorkflowContext context) {
        if (conditions.isEmpty()) {
            return false;
        }
        boolean isAnd = !"OR".equalsIgnoreCase(logic);
        if (isAnd) {
            for (Map<String, Object> cond : conditions) {
                if (!evaluateSingle(cond, context)) {
                    return false;
                }
            }
            return true;
        }
        for (Map<String, Object> cond : conditions) {
            if (evaluateSingle(cond, context)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateSingle(Map<String, Object> cond, WorkflowContext context) {
        String op = cond.get("operator") == null ? "eq" : String.valueOf(cond.get("operator"));
        String left = templateRenderer.render(String.valueOf(cond.getOrDefault("left", "")), context);
        String right = templateRenderer.render(String.valueOf(cond.getOrDefault("right", "")), context);
        return switch (op) {
            case "eq" -> left.equals(right);
            case "ne" -> !left.equals(right);
            case "contains" -> left.contains(right);
            case "not_contains" -> !left.contains(right);
            case "gt" -> compareNumber(left, right) > 0;
            case "gte" -> compareNumber(left, right) >= 0;
            case "lt" -> compareNumber(left, right) < 0;
            case "lte" -> compareNumber(left, right) <= 0;
            case "empty" -> StrUtil.isBlank(left);
            case "not_empty" -> StrUtil.isNotBlank(left);
            default -> false;
        };
    }

    private int compareNumber(String left, String right) {
        try {
            double l = Double.parseDouble(left);
            double r = Double.parseDouble(right);
            return Double.compare(l, r);
        } catch (NumberFormatException ex) {
            return left.compareTo(right);
        }
    }
}
