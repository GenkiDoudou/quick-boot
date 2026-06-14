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
 * 内置「批处理节点测试」模板：数组批处理 + 模板转换，汇总 result 数组。
 * <p>
 * 不依赖 LLM；start 输入 {@code items} 字符串数组，批处理体内按元素生成带索引的文本。
 */
@Component
public class BatchTestWorkflowTemplate {

    /**
     * 构建批处理测试模板元数据与图 DSL。
     *
     * @return 模板 VO
     */
    public WfTemplateVo build() {
        WfTemplateVo vo = new WfTemplateVo();
        vo.setCode(WorkflowConstants.TEMPLATE_BATCH_ARRAY_TEST);
        vo.setName("批处理节点测试（数组）");
        vo.setDescription(
            "start → batch(并行) → 聚合 result 数组；批处理体内 template-transform，无需大模型");
        vo.setGraph(buildGraph());
        return vo;
    }

    private WorkflowGraphDto buildGraph() {
        WorkflowGraphDto graph = new WorkflowGraphDto();
        graph.setVersion(WorkflowConstants.GRAPH_VERSION);

        GraphNodeDto start = node("start_1", "start", 80, 200, Map.of(
            "inputs", List.of(Map.of(
                "key", "items",
                "type", "array",
                "required", false,
                "label", "待处理列表",
                "description", "字符串数组，默认 [a,b,c]"
            ))
        ));

        GraphNodeDto batch = node("batch_1", "batch", 300, 120, Map.of(
            "bodyId", "batch_body_1",
            "parallelLimit", 3,
            "maxRuns", 100,
            "inputParameters", List.of(Map.of(
                "key", "item",
                "source", "{{start_1.items}}"
            )),
            "outputParameters", List.of(Map.of(
                "key", "result",
                "nodeId", "tmpl_1",
                "field", "result"
            ))
        ));

        GraphNodeDto batchBody = node("batch_body_1", "batch-body", 260, 200, Map.of(
            "batchNodeId", "batch_1",
            "width", 360,
            "height", 180
        ));

        GraphNodeDto tmpl = childNode("tmpl_1", "template-transform", "batch_body_1", 40, 48, Map.of(
            "template", "[{{batch_1.index}}] {{batch_1.item}}"
        ));

        GraphNodeDto answerMain = node("answer_main", "answer", 720, 200, Map.of(
            "outputMode", "variables",
            "outputVariables", List.of(
                Map.of("key", "results", "value", "{{batch_1.result}}"),
                Map.of("key", "count", "value", "{{batch_1.count}}")
            ),
            "output", "",
            "streaming", false
        ));

        GraphNodeDto end = node("end_1", "end", 920, 200, Map.of(
            "outputMode", "variables",
            "outputVariables", List.of(
                Map.of("key", "results", "value", "{{answer_main.results}}"),
                Map.of("key", "count", "value", "{{answer_main.count}}")
            ),
            "output", "",
            "streaming", false
        ));

        graph.setNodes(List.of(start, batch, batchBody, tmpl, answerMain, end));
        graph.setEdges(List.of(
            edge("e_start_batch", "start_1", "batch_1", null),
            edge("e_batch_answer", "batch_1", "answer_main", null),
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
}
