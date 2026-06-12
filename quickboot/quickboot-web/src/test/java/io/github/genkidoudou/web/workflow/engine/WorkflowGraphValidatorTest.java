package io.github.genkidoudou.web.workflow.engine;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.constants.WorkflowConstants;
import io.github.genkidoudou.web.workflow.dto.GraphEdgeDto;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link WorkflowGraphValidator} 单元测试。
 */
class WorkflowGraphValidatorTest {

    private WorkflowGraphValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorkflowGraphValidator();
    }

    @Test
    void validate_linearRagGraph_success() {
        WorkflowGraphDto graph = linearRagGraph();
        assertDoesNotThrow(() -> validator.validate(graph));
    }

    @Test
    void validate_missingStart_throws() {
        WorkflowGraphDto graph = linearRagGraph();
        graph.getNodes().removeIf(n -> WfNodeType.START.equals(n.getType()));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(graph));
        assertTrue(ex.getMessage().contains("start"));
    }

    @Test
    void validate_ifElseMissingBranch_throws() {
        WorkflowGraphDto graph = linearRagGraph();
        GraphNodeDto ifElse = node("if_1", WfNodeType.IF_ELSE);
        graph.getNodes().add(ifElse);
        graph.getEdges().add(edge("e_if", "start_1", "if_1", null));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(graph));
        assertTrue(ex.getMessage().contains("if-else"));
    }

    private WorkflowGraphDto linearRagGraph() {
        WorkflowGraphDto graph = new WorkflowGraphDto();
        graph.setVersion(WorkflowConstants.GRAPH_VERSION);
        graph.setNodes(new ArrayList<>(List.of(
            node("start_1", WfNodeType.START),
            node("kb_1", WfNodeType.KNOWLEDGE_RETRIEVAL),
            node("llm_1", WfNodeType.LLM),
            node("answer_1", WfNodeType.ANSWER)
        )));
        graph.setEdges(new ArrayList<>(List.of(
            edge("e1", "start_1", "kb_1", null),
            edge("e2", "kb_1", "llm_1", null),
            edge("e3", "llm_1", "answer_1", null)
        )));
        return graph;
    }

    private GraphNodeDto node(String id, String type) {
        GraphNodeDto node = new GraphNodeDto();
        node.setId(id);
        node.setType(type);
        node.setData(new HashMap<>());
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
