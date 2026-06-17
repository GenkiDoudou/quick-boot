package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.InputParameterTemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JsonSerializeNodeHandler} 单元测试。
 */
class JsonSerializeNodeHandlerTest {

    private JsonSerializeNodeHandler handler;

    @BeforeEach
    void setUp() {
        handler = new JsonSerializeNodeHandler(new InputParameterTemplateRenderer(new TemplateRenderer()));
    }

    @Test
    void execute_resolvesInputAndSerializesObject() {
        WorkflowContext context = new WorkflowContext(1L, "start_1");
        Map<String, Object> llmOut = new HashMap<>();
        llmOut.put("output", Map.of("code", 0));
        context.putNodeOutputs("llm_1", llmOut);

        GraphNodeDto node = new GraphNodeDto();
        node.setId("json_1");
        node.setType(WfNodeType.JSON_SERIALIZE);
        node.setData(Map.of(
            "inputVariables", List.of(Map.of(
                "key", "input",
                "value", "{{llm_1.output}}"
            ))
        ));

        NodeResult result = handler.execute(node, context);
        assertTrue(result.isSuccess());
        assertEquals("{\"code\":0}", result.getOutputs().get("output"));
        assertEquals("input", result.getTraceInputs().get("inputKey"));
    }
}
