package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 循环体出口锚点：无业务逻辑，仅用于体内 DAG 连线的终点。
 */
@Component
public class LoopBodyEndNodeHandler implements NodeHandler {

    @Override
    public String type() {
        return WfNodeType.LOOP_BODY_END;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        return NodeResult.success(Map.of());
    }
}
