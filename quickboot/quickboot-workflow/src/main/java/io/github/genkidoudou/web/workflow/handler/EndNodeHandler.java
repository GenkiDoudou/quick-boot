package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.OutputNodeExecutor;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

/**
 * 结束节点：工作流固定出口，配置与「输出」节点相同的返回变量/返回文本，作为 API 最终结果。
 */
@Component
public class EndNodeHandler implements NodeHandler {

    private final OutputNodeExecutor outputNodeExecutor;

    public EndNodeHandler(OutputNodeExecutor outputNodeExecutor) {
        this.outputNodeExecutor = outputNodeExecutor;
    }

    @Override
    public String type() {
        return WfNodeType.END;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        return NodeResult.success(outputNodeExecutor.execute(node, context));
    }
}
