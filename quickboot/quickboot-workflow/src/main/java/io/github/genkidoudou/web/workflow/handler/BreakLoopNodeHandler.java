package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.LoopExecutionScope;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 终止循环节点：立即跳出循环体所在循环，常用于无限循环模式。
 */
@Component
public class BreakLoopNodeHandler implements NodeHandler {

    @Override
    public String type() {
        return WfNodeType.BREAK_LOOP;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        LoopExecutionScope scope = context.getCurrentLoopScope();
        if (scope == null) {
            return NodeResult.failed("终止循环节点只能在循环体内使用");
        }
        scope.setBreakRequested(true);
        return NodeResult.success(Map.of("broken", true));
    }
}
