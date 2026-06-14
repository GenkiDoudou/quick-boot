package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 循环体入口锚点：无业务逻辑，仅用于体内 DAG 连线的起点。
 */
@Component
public class LoopBodyStartNodeHandler implements NodeHandler {

    @Override
    public String type() {
        return WfNodeType.LOOP_BODY_START;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        return NodeResult.success(Map.of());
    }
}
