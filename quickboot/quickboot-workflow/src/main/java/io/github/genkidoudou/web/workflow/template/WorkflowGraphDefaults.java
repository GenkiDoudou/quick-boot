package io.github.genkidoudou.web.workflow.template;

import io.github.genkidoudou.web.workflow.constants.WorkflowConstants;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流默认图：新建时仅含固定的开始、结束节点。
 */
public final class WorkflowGraphDefaults {

    private WorkflowGraphDefaults() {
    }

    /**
     * 最小默认图（开始 + 结束），输出节点由用户在画布中添加。
     *
     * @return 图 DSL
     */
    public static WorkflowGraphDto minimal() {
        WorkflowGraphDto graph = new WorkflowGraphDto();
        graph.setVersion(WorkflowConstants.GRAPH_VERSION);
        graph.setNodes(List.of(
            node("start_1", "start", 80, 280, Map.of("inputs", List.of())),
            node("end_1", "end", 720, 280, Map.of(
                "outputMode", "variables",
                "outputVariables", List.of(),
                "output", "",
                "streaming", false
            ))
        ));
        graph.setEdges(List.of());
        return graph;
    }

    private static GraphNodeDto node(String id, String type, int x, int y, Map<String, Object> data) {
        GraphNodeDto node = new GraphNodeDto();
        node.setId(id);
        node.setType(type);
        node.setPosition(Map.of("x", x, "y", y));
        node.setData(new HashMap<>(data));
        return node;
    }
}
