package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 批处理体容器节点：仅作画布分组，主引擎不调度。
 */
@Component
public class BatchBodyNodeHandler implements NodeHandler {

    @Override
    public String type() {
        return WfNodeType.BATCH_BODY;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        return NodeResult.success(Map.of());
    }
}
