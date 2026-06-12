package io.github.genkidoudou.web.workflow.engine;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link NodeHandler} 注册表，按节点 type 调度 Handler。
 */
@Component
public class NodeHandlerRegistry {

    private final Map<String, NodeHandler> byType;

    public NodeHandlerRegistry(List<NodeHandler> handlers) {
        Map<String, NodeHandler> map = new HashMap<>();
        if (handlers != null) {
            for (NodeHandler handler : handlers) {
                map.put(handler.type(), handler);
            }
        }
        this.byType = Map.copyOf(map);
    }

    /**
     * 获取指定类型的 Handler，未注册时抛出业务异常。
     *
     * @param type 节点类型
     * @return Handler 实例
     */
    public NodeHandler require(String type) {
        NodeHandler handler = byType.get(type);
        if (handler == null) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_GRAPH_INVALID, "未注册的节点类型: " + type);
        }
        return handler;
    }
}
