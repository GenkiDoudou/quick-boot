package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
import io.github.genkidoudou.web.workflow.engine.LoopSubgraphExecutor;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.stream.WorkflowStreamEmitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link LoopNodeHandler} 数组循环测试。
 */
@ExtendWith(MockitoExtension.class)
class LoopNodeHandlerTest {

    @Mock
    private LoopSubgraphExecutor loopSubgraphExecutor;

    @Mock
    private WorkflowStreamEmitter streamEmitter;

    private LoopNodeHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LoopNodeHandler(new TemplateRenderer(), loopSubgraphExecutor, streamEmitter);
    }

    @Test
    void execute_arrayLoop_collectsAnswerOutputVariablesIntoRes() {
        WorkflowContext context = new WorkflowContext(1L, "start_1");
        Map<String, Object> startOut = new HashMap<>();
        startOut.put("nums", List.of(12, 34, 56));
        context.putNodeOutputs("start_1", startOut);
        context.setExecutionGraph(new WorkflowGraphDto());

        GraphNodeDto loopNode = new GraphNodeDto();
        loopNode.setId("loop_1");
        loopNode.setType(WfNodeType.LOOP);
        loopNode.setData(Map.of(
            "loopType", "array",
            "bodyId", "loop_body_1",
            "outputVariableName", "res",
            "outputNodeId", "answer_in_1",
            "outputField", "text",
            "arrayParameters", List.of(Map.of(
                "key", "item",
                "source", "{{start_1.nums}}"
            )),
            "outputMode", "results"
        ));

        when(loopSubgraphExecutor.executeIteration(any(), any(), any()))
            .thenAnswer(invocation -> {
                WorkflowContext ctx = invocation.getArgument(2);
                Map<String, Object> loopOut = ctx.getNodeOutputMap("loop_1");
                Map<String, Object> answerOut = new HashMap<>();
                answerOut.put("item", loopOut.get("item"));
                answerOut.put("index", loopOut.get("index"));
                ctx.putNodeOutputs("answer_in_1", answerOut);
                return io.github.genkidoudou.web.workflow.engine.LoopIterationResult.NORMAL;
            });

        NodeResult result = handler.execute(loopNode, context);
        Map<String, Object> outputs = result.getOutputs();

        assertEquals(3, outputs.get("count"));
        @SuppressWarnings("unchecked")
        List<Object> res = (List<Object>) outputs.get("res");
        assertEquals(3, res.size());
        @SuppressWarnings("unchecked")
        Map<String, Object> lastRound = (Map<String, Object>) res.get(2);
        assertEquals(56, lastRound.get("item"));
        assertEquals(2, lastRound.get("index"));
        assertEquals(false, outputs.containsKey("results"));
    }

    @Test
    void execute_arrayLoopFromStartNode_runsThreeIterations() {
        when(loopSubgraphExecutor.executeIteration(any(), any(), any()))
            .thenReturn(io.github.genkidoudou.web.workflow.engine.LoopIterationResult.NORMAL);

        WorkflowContext context = new WorkflowContext(1L, "start_1");
        Map<String, Object> startOut = new HashMap<>();
        startOut.put("nums", List.of(12, 34, 56));
        context.putNodeOutputs("start_1", startOut);
        context.setExecutionGraph(new WorkflowGraphDto());

        GraphNodeDto loopNode = new GraphNodeDto();
        loopNode.setId("loop_1");
        loopNode.setType(WfNodeType.LOOP);
        loopNode.setData(Map.of(
            "loopType", "array",
            "bodyId", "loop_body_1",
            "arrayParameters", List.of(Map.of(
                "key", "item",
                "source", "{{start_1.nums}}"
            )),
            "outputMode", "results"
        ));

        NodeResult result = handler.execute(loopNode, context);
        Map<String, Object> outputs = result.getOutputs();

        assertEquals(3, outputs.get("count"));
        assertEquals(2, outputs.get("index"));
        assertEquals(List.of(), outputs.get("results"));
    }
}
