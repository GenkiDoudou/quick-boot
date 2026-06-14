package io.github.genkidoudou.web.workflow.engine;

import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * {@link OutputNodeExecutor} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class OutputNodeExecutorTest {

    @Mock
    private WorkflowStreamEmitter streamEmitter;

    private OutputNodeExecutor executor;
    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        executor = new OutputNodeExecutor(new TemplateRenderer(), streamEmitter);
        context = new WorkflowContext(1L, "start_1");
        List<Map<String, Object>> loopRes = List.of(
            Map.of("index", 0, "item", 12),
            Map.of("index", 1, "item", 34),
            Map.of("index", 2, "item", 56)
        );
        Map<String, Object> loopOut = new HashMap<>();
        loopOut.put("res", loopRes);
        loopOut.put("count", 3);
        context.putNodeOutputs("loop_1", loopOut);
    }

    @Test
    void executeVariablesMode_singlePlaceholder_returnsArrayObject() {
        GraphNodeDto endNode = buildEndNode(List.of(
            Map.of("key", "res", "value", "{{loop_1.res}}"),
            Map.of("key", "count", "value", "{{loop_1.count}}")
        ));

        Map<String, Object> outputs = executor.execute(endNode, context);

        assertInstanceOf(List.class, outputs.get("res"));
        assertEquals(3, ((List<?>) outputs.get("res")).size());
        assertEquals(3, outputs.get("count"));
    }

    @Test
    void executeVariablesMode_duplicatePlaceholders_returnsSingleArrayNotConcatenatedString() {
        GraphNodeDto endNode = buildEndNode(List.of(
            Map.of("key", "res", "value", "{{loop_1.res}}{{loop_1.res}}")
        ));

        Map<String, Object> outputs = executor.execute(endNode, context);

        assertInstanceOf(List.class, outputs.get("res"));
        assertEquals(3, ((List<?>) outputs.get("res")).size());
    }

    private GraphNodeDto buildEndNode(List<Map<String, String>> outputVariables) {
        GraphNodeDto node = new GraphNodeDto();
        node.setId("end_1");
        node.setData(Map.of(
            "outputMode", "variables",
            "outputVariables", outputVariables
        ));
        return node;
    }
}
