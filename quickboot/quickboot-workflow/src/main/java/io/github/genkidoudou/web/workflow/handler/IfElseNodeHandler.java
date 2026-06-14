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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 条件分支（选择器）节点：按优先级依次评估多个条件分支，命中则走对应出口；均不满足走「否则」。
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
        List<Map<String, Object>> branches = resolveBranches(data);

        for (Map<String, Object> branch : branches) {
            List<Map<String, Object>> conditions = branch.get("conditions") instanceof List<?> list
                ? (List<Map<String, Object>>) list : List.of();
            String logic = branch.get("logic") == null ? "AND" : String.valueOf(branch.get("logic"));
            if (evaluateConditions(conditions, logic, context)) {
                String handle = resolveBranchHandle(branch);
                Map<String, Object> outputs = new HashMap<>();
                outputs.put("branch", handle);
                return NodeResult.successWithBranch(outputs, handle);
            }
        }

        Map<String, Object> outputs = new HashMap<>();
        outputs.put("branch", WorkflowConstants.HANDLE_FALSE);
        return NodeResult.successWithBranch(outputs, WorkflowConstants.HANDLE_FALSE);
    }

    /**
     * 解析分支列表；兼容旧版根级 conditions + logic（视为单一「如果」分支）。
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> resolveBranches(Map<String, Object> data) {
        if (data.get("branches") instanceof List<?> branchList && !branchList.isEmpty()) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : branchList) {
                if (item instanceof Map<?, ?> map) {
                    result.add((Map<String, Object>) map);
                }
            }
            return result;
        }
        List<Map<String, Object>> conditions = data.get("conditions") instanceof List<?> list
            ? (List<Map<String, Object>>) list : List.of();
        if (conditions.isEmpty()) {
            return List.of();
        }
        Map<String, Object> legacyBranch = new HashMap<>();
        legacyBranch.put("id", WorkflowConstants.HANDLE_TRUE);
        legacyBranch.put("name", "如果");
        legacyBranch.put("logic", data.get("logic") == null ? "AND" : String.valueOf(data.get("logic")));
        legacyBranch.put("conditions", conditions);
        return List.of(legacyBranch);
    }

    private String resolveBranchHandle(Map<String, Object> branch) {
        Object id = branch.get("id");
        if (id != null && StrUtil.isNotBlank(String.valueOf(id))) {
            return String.valueOf(id).trim();
        }
        return WorkflowConstants.HANDLE_TRUE;
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
        String op = normalizeOperator(cond.get("operator") == null ? "eq" : String.valueOf(cond.get("operator")));
        String left = templateRenderer.render(String.valueOf(cond.getOrDefault("left", "")), context);
        String right = templateRenderer.render(String.valueOf(cond.getOrDefault("right", "")), context);
        int leftLen = left == null ? 0 : left.length();
        int rightLen = parseLength(right);
        return switch (op) {
            case "eq" -> left.equals(right);
            case "ne" -> !left.equals(right);
            case "contains" -> left.contains(right);
            case "not_contains" -> !left.contains(right);
            case "gt" -> compareNumber(left, right) > 0;
            case "gte" -> compareNumber(left, right) >= 0;
            case "lt" -> compareNumber(left, right) < 0;
            case "lte" -> compareNumber(left, right) <= 0;
            case "length_gt" -> leftLen > rightLen;
            case "length_eq" -> leftLen == rightLen;
            case "length_lt" -> leftLen < rightLen;
            case "length_lte" -> leftLen <= rightLen;
            case "has_key" -> objectHasKey(left, right);
            case "not_has_key" -> !objectHasKey(left, right);
            case "empty" -> StrUtil.isBlank(left);
            case "not_empty" -> StrUtil.isNotBlank(left);
            default -> false;
        };
    }

    private int parseLength(String value) {
        if (StrUtil.isBlank(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private boolean objectHasKey(String left, String key) {
        if (StrUtil.isBlank(key) || StrUtil.isBlank(left)) {
            return false;
        }
        String k = key.trim();
        try {
            if (cn.hutool.json.JSONUtil.isTypeJSONObject(left)) {
                return cn.hutool.json.JSONUtil.parseObj(left).containsKey(k);
            }
        } catch (Exception ignored) {
            // 非 JSON 对象时视为不包含键
        }
        return false;
    }

    private String normalizeOperator(String op) {
        if ("not-empty".equals(op)) {
            return "not_empty";
        }
        if ("neq".equals(op)) {
            return "ne";
        }
        return op;
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
