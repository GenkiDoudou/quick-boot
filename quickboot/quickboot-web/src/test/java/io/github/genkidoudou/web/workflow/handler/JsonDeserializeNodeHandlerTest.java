package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.InputParameterTemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JsonDeserializeNodeHandler} 单元测试。
 */
class JsonDeserializeNodeHandlerTest {

    private JsonDeserializeNodeHandler handler;

    @BeforeEach
    void setUp() {
        handler = new JsonDeserializeNodeHandler(new InputParameterTemplateRenderer(new TemplateRenderer()));
    }

    @Test
    void execute_wholeObject_success() {
        WorkflowContext context = new WorkflowContext(1L, "start_1");
        context.putNodeOutputs("http_1", Map.of("body", "{\"name\":\"张三\"}"));

        GraphNodeDto node = new GraphNodeDto();
        node.setId("json_deser_1");
        node.setType(WfNodeType.JSON_DESERIALIZE);
        node.setData(Map.of(
            "inputVariables", List.of(Map.of("key", "input", "value", "{{http_1.body}}")),
            "outputFields", List.of()
        ));

        NodeResult result = handler.execute(node, context);
        assertTrue(result.isSuccess());
        assertEquals("张三", ((Map<?, ?>) result.getOutputs().get("output")).get("name"));
        assertEquals(0, result.getTraceInputs().get("fieldCount"));
    }

    @Test
    void execute_withFields_extractsValues() {
        WorkflowContext context = new WorkflowContext(1L, "start_1");
        context.putNodeOutputs("http_1", Map.of(
            "body", "{\"data\":{\"user\":{\"name\":\"张三\",\"age\":18}}}"
        ));

        GraphNodeDto node = new GraphNodeDto();
        node.setId("json_deser_1");
        node.setType(WfNodeType.JSON_DESERIALIZE);
        node.setData(Map.of(
            "inputVariables", List.of(Map.of("key", "input", "value", "{{http_1.body}}")),
            "outputFields", List.of(
                Map.of("key", "name", "path", "data.user.name", "type", "string"),
                Map.of("key", "age", "path", "data.user.age", "type", "number")
            )
        ));

        NodeResult result = handler.execute(node, context);
        assertTrue(result.isSuccess());
        Map<?, ?> output = (Map<?, ?>) result.getOutputs().get("output");
        assertEquals("张三", output.get("name"));
        assertEquals(18, output.get("age"));
        assertEquals(2, result.getTraceInputs().get("fieldCount"));
    }

    @Test
    void execute_invalidJson_failed() {
        GraphNodeDto node = baseNode("{bad}");
        NodeResult result = handler.execute(node, new WorkflowContext(1L, "start_1"));
        assertFalse(result.isSuccess());
    }

    @Test
    void execute_emptyInput_failed() {
        WorkflowContext context = new WorkflowContext(1L, "start_1");
        context.putNodeOutputs("http_1", Map.of("body", ""));

        GraphNodeDto node = baseNode("{{http_1.body}}");
        NodeResult result = handler.execute(node, context);
        assertFalse(result.isSuccess());
    }

    @Test
    void execute_missingPath_successWithNull() {
        WorkflowContext context = new WorkflowContext(1L, "start_1");
        context.putNodeOutputs("http_1", Map.of("body", "{\"name\":\"张三\"}"));

        GraphNodeDto node = new GraphNodeDto();
        node.setId("json_deser_1");
        node.setType(WfNodeType.JSON_DESERIALIZE);
        node.setData(Map.of(
            "inputVariables", List.of(Map.of("key", "input", "value", "{{http_1.body}}")),
            "outputFields", List.of(Map.of("key", "missing", "path", "not.exist", "type", "string"))
        ));

        NodeResult result = handler.execute(node, context);
        assertTrue(result.isSuccess());
        assertNull(((Map<?, ?>) result.getOutputs().get("output")).get("missing"));
    }

    @Test
    void execute_fieldsWithArrayRoot_failed() {
        WorkflowContext context = new WorkflowContext(1L, "start_1");
        context.putNodeOutputs("http_1", Map.of("body", "[1,2,3]"));

        GraphNodeDto node = new GraphNodeDto();
        node.setId("json_deser_1");
        node.setType(WfNodeType.JSON_DESERIALIZE);
        node.setData(Map.of(
            "inputVariables", List.of(Map.of("key", "input", "value", "{{http_1.body}}")),
            "outputFields", List.of(Map.of("key", "x", "path", "x", "type", "string"))
        ));

        NodeResult result = handler.execute(node, context);
        assertFalse(result.isSuccess());
    }

    private GraphNodeDto baseNode(String inputValue) {
        GraphNodeDto node = new GraphNodeDto();
        node.setId("json_deser_1");
        node.setType(WfNodeType.JSON_DESERIALIZE);
        node.setData(Map.of(
            "inputVariables", List.of(Map.of("key", "input", "value", inputValue)),
            "outputFields", List.of()
        ));
        return node;
    }
}
