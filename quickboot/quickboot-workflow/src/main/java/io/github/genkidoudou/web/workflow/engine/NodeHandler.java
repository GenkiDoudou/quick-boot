package io.github.genkidoudou.web.workflow.engine;

import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;

import java.util.Map;

/**
 * 工作流节点 Handler 接口：每种节点类型一个实现。
 */
public interface NodeHandler {

    /**
     * 支持的节点类型。
     *
     * @return 节点 type 字符串
     */
    String type();

    /**
     * 执行节点逻辑。
     *
     * @param node    画布节点定义
     * @param context 运行时上下文
     * @return 节点执行结果
     */
    NodeResult execute(GraphNodeDto node, WorkflowContext context);
}
