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
 * 继续循环节点：跳过本轮剩余节点，进入下一轮迭代。
 */
@Component
public class ContinueLoopNodeHandler implements NodeHandler {

    @Override
    public String type() {
        return WfNodeType.CONTINUE_LOOP;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        LoopExecutionScope scope = context.getCurrentLoopScope();
        if (scope == null) {
            return NodeResult.failed("继续循环节点只能在循环体内使用");
        }
        scope.setContinueRequested(true);
        return NodeResult.success(Map.of("continued", true));
    }
}
