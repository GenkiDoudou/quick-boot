package io.github.genkidoudou.web.workflow.template;

import io.github.genkidoudou.web.workflow.constants.WorkflowConstants;
import io.github.genkidoudou.web.workflow.dto.GraphEdgeDto;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.dto.WfTemplateVo;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内置「循环节点测试」模板：指定次数循环 + 模板转换 + 变量赋值 + 结果聚合。
 * <p>
 * 不依赖 LLM / 知识库，可直接 Debug 运行；输入 {@code prefix}（可选，默认 Loop）。
 */
@Component
public class LoopTestWorkflowTemplate {

    /**
     * 构建循环测试模板元数据与图 DSL。
     *
     * @return 模板 VO
     */
    public WfTemplateVo build() {
        WfTemplateVo vo = new WfTemplateVo();
        vo.setCode(WorkflowConstants.TEMPLATE_LOOP_COUNT_TEST);
        vo.setName("循环节点测试（指定次数）");
        vo.setDescription(
            "start → loop(3次) → 聚合 results；循环体内 template-transform + variable-assign + answer，无需大模型");
        vo.setGraph(buildGraph());
        return vo;
    }

    private WorkflowGraphDto buildGraph() {
        WorkflowGraphDto graph = new WorkflowGraphDto();
        graph.setVersion(WorkflowConstants.GRAPH_VERSION);

        GraphNodeDto start = node("start_1", "start", 80, 200, Map.of(
            "inputs", List.of(Map.of(
                "key", "prefix",
                "type", "string",
                "required", false,
                "label", "前缀",
                "description", "每轮输出前缀，默认 Loop"
            ))
        ));

        GraphNodeDto loop = node("loop_1", "loop", 300, 120, Map.of(
            "loopType", "count",
            "count", 3,
            "bodyId", "loop_body_1",
            "outputMode", "results",
            "outputNodeId", "answer_in_1",
            "outputField", "round",
            "intermediateVariables", List.of(Map.of("key", "snap", "initialValue", ""))
        ));

        GraphNodeDto loopBody = node("loop_body_1", "loop-body", 260, 200, Map.of(
            "loopNodeId", "loop_1",
            "width", 400,
            "height", 220
        ));

        GraphNodeDto tmpl = childNode("tmpl_1", "template-transform", "loop_body_1", 40, 48, Map.of(
            "template", "[{{loop_1.index}}] {{start_1.prefix}} item={{loop_1.item}}"
        ));

        GraphNodeDto assign = childNode("assign_1", "loop-set-variable", "loop_body_1", 40, 120, Map.of(
            "target", "snap",
            "value", "{{tmpl_1.result}}"
        ));

        GraphNodeDto answerIn = childNode("answer_in_1", "answer", "loop_body_1", 220, 48, Map.of(
            "outputMode", "variables",
            "outputVariables", List.of(Map.of(
                "key", "round",
                "value", "{{tmpl_1.result}}"
            )),
            "output", "",
            "streaming", false
        ));

        GraphNodeDto answerMain = node("answer_main", "answer", 720, 200, Map.of(
            "outputMode", "variables",
            "outputVariables", List.of(
                Map.of("key", "results", "value", "{{loop_1.results}}"),
                Map.of("key", "count", "value", "{{loop_1.count}}"),
                Map.of("key", "lastSnap", "value", "{{loop_1.snap}}")
            ),
            "output", "",
            "streaming", false
        ));

        GraphNodeDto end = node("end_1", "end", 920, 200, Map.of(
            "outputMode", "variables",
            "outputVariables", List.of(
                Map.of("key", "results", "value", "{{answer_main.results}}"),
                Map.of("key", "count", "value", "{{answer_main.count}}"),
                Map.of("key", "lastSnap", "value", "{{answer_main.lastSnap}}")
            ),
            "output", "",
            "streaming", false
        ));

        graph.setNodes(List.of(start, loop, loopBody, tmpl, assign, answerIn, answerMain, end));
        graph.setEdges(List.of(
            edge("e_start_loop", "start_1", "loop_1", null),
            bodyEdge("e_loop_body", "loop_1", "loop_body_1", WorkflowConstants.LOOP_HANDLE_BODY),
            bodyEdge("e_body_entry", "loop_body_1", "tmpl_1", WorkflowConstants.LOOP_BODY_HANDLE_ENTRY),
            edge("e_tmpl_assign", "tmpl_1", "assign_1", null),
            edge("e_assign_answer_in", "assign_1", "answer_in_1", null),
            bodyEdge("e_body_exit", "answer_in_1", "loop_body_1", null),
            edge("e_loop_answer", "loop_1", "answer_main", WorkflowConstants.LOOP_HANDLE_FLOW_OUT),
            edge("e_answer_end", "answer_main", "end_1", null)
        ));
        return graph;
    }

    private GraphNodeDto node(String id, String type, int x, int y, Map<String, Object> data) {
        GraphNodeDto node = new GraphNodeDto();
        node.setId(id);
        node.setType(type);
        node.setPosition(Map.of("x", x, "y", y));
        node.setData(new HashMap<>(data));
        return node;
    }

    private GraphNodeDto childNode(String id, String type, String parentId, int x, int y, Map<String, Object> data) {
        GraphNodeDto node = node(id, type, x, y, data);
        node.setParentId(parentId);
        return node;
    }

    private GraphEdgeDto edge(String id, String source, String target, String handle) {
        GraphEdgeDto edge = new GraphEdgeDto();
        edge.setId(id);
        edge.setSource(source);
        edge.setTarget(target);
        edge.setSourceHandle(handle);
        return edge;
    }

    private GraphEdgeDto bodyEdge(String id, String source, String target, String sourceHandle) {
        return edge(id, source, target, sourceHandle);
    }
}
