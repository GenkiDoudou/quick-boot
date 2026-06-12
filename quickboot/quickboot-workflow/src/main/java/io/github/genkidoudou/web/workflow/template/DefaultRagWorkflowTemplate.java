package io.github.genkidoudou.web.workflow.template;

import io.github.genkidoudou.web.workflow.constants.WorkflowConstants;
import io.github.genkidoudou.web.workflow.dto.GraphEdgeDto;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.dto.WfTemplateVo;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内置「默认 RAG 工作流」模板：start → knowledge-retrieval → llm → answer。
 */
@Component
public class DefaultRagWorkflowTemplate {

    /**
     * 构建默认 RAG 模板元数据与图 DSL。
     *
     * @return 模板 VO
     */
    public WfTemplateVo build() {
        WfTemplateVo vo = new WfTemplateVo();
        vo.setCode(WorkflowConstants.TEMPLATE_DEFAULT_RAG);
        vo.setName("默认 RAG 工作流");
        vo.setDescription("等价于 /knowledge/chat 的 start → 知识检索 → LLM → 回答 链路，可复制后修改");
        vo.setGraph(buildGraph());
        return vo;
    }

    private WorkflowGraphDto buildGraph() {
        WorkflowGraphDto graph = new WorkflowGraphDto();
        graph.setVersion(WorkflowConstants.GRAPH_VERSION);

        GraphNodeDto start = node("start_1", "start", 80, 200, Map.of(
            "inputs", List.of(Map.of(
                "key", "question",
                "type", "string",
                "required", true,
                "label", "用户问题"
            ))
        ));
        GraphNodeDto kb = node("kb_1", "knowledge-retrieval", 320, 200, Map.of(
            "kbId", "{{sys.kbId}}",
            "query", "{{start_1.question}}",
            "topK", 5,
            "similarityThreshold", 0.65
        ));
        GraphNodeDto llm = node("llm_1", "llm", 560, 200, Map.of(
            "systemPrompt", "你是企业助手，仅依据上下文回答。",
            "userPrompt", "问题：{{start_1.question}}\n\n上下文：{{kb_1.contextText}}",
            "temperature", 0.3,
            "streaming", true
        ));
        GraphNodeDto answer = node("answer_1", "answer", 800, 200, Map.of(
            "outputMode", "text",
            "output", "{{llm_1.text}}",
            "outputVariables", List.of(),
            "streaming", false,
            "citations", "{{kb_1.citations}}"
        ));

        graph.setNodes(List.of(start, kb, llm, answer));
        graph.setEdges(List.of(
            edge("e1", "start_1", "kb_1", null),
            edge("e2", "kb_1", "llm_1", null),
            edge("e3", "llm_1", "answer_1", null)
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

    private GraphEdgeDto edge(String id, String source, String target, String handle) {
        GraphEdgeDto edge = new GraphEdgeDto();
        edge.setId(id);
        edge.setSource(source);
        edge.setTarget(target);
        edge.setSourceHandle(handle);
        return edge;
    }
}
