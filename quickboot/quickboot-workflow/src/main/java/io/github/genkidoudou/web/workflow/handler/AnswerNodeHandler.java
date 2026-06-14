package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.OutputNodeExecutor;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

/**
 * 输出（answer）节点：中间结果输出，可添加多个。
 */
@Component
public class AnswerNodeHandler implements NodeHandler {

    private final OutputNodeExecutor outputNodeExecutor;

    public AnswerNodeHandler(OutputNodeExecutor outputNodeExecutor) {
        this.outputNodeExecutor = outputNodeExecutor;
    }

    @Override
    public String type() {
        return WfNodeType.ANSWER;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        return NodeResult.success(outputNodeExecutor.execute(node, context));
    }
}
