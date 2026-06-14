package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.LoopExecutionScope;
import io.github.genkidoudou.web.workflow.engine.LoopVariableTypeChecker;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 设置变量节点（循环体专用）：更新循环节点配置的中间变量，供下一轮迭代引用。
 */
@Component
public class LoopSetVariableNodeHandler implements NodeHandler {

    private final TemplateRenderer templateRenderer;

    public LoopSetVariableNodeHandler(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.LOOP_SET_VARIABLE;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        LoopExecutionScope scope = context.getCurrentLoopScope();
        if (scope == null) {
            return NodeResult.failed("设置变量节点只能在循环体内使用");
        }
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String target = data.get("target") == null ? "" : String.valueOf(data.get("target")).trim();
        if (StrUtil.isBlank(target)) {
            return NodeResult.failed("设置变量节点未指定中间变量");
        }
        if (!scope.getIntermediateKeys().contains(target)) {
            return NodeResult.failed("中间变量 " + target + " 未在循环节点中声明");
        }
        Object valueTemplate = data.get("value");
        Object resolved = valueTemplate == null ? ""
            : templateRenderer.resolveObject(String.valueOf(valueTemplate), context);
        String expectedType = scope.getIntermediateTypes().getOrDefault(target, "any");
        String typeError = LoopVariableTypeChecker.incompatibilityMessage(expectedType, resolved);
        if (typeError != null) {
            return NodeResult.failed(typeError);
        }
        scope.getIntermediateVars().put(target, resolved);
        context.putNodeOutput(scope.getLoopNodeId(), target, resolved);
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(target, resolved);
        return NodeResult.success(outputs);
    }
}
