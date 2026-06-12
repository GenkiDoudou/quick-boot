package io.github.genkidoudou.web.workflow.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link TemplateRenderer} 单元测试。
 */
class TemplateRendererTest {

    private TemplateRenderer renderer;
    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        renderer = new TemplateRenderer();
        context = new WorkflowContext(1L, "start_1");
        context.getRunInputs().put("question", "什么是 QuickBoot？");
        context.getSysVariables().put("kbId", 100L);
        context.getSysVariables().put("userId", "admin");
        Map<String, Object> kbOutput = new HashMap<>();
        kbOutput.put("contextText", "QuickBoot 是企业级脚手架");
        context.putNodeOutputs("kb_1", kbOutput);
    }

    @Test
    void render_nodeFieldPlaceholder() {
        String result = renderer.render("上下文：{{kb_1.contextText}}", context);
        assertEquals("上下文：QuickBoot 是企业级脚手架", result);
    }

    @Test
    void render_sysVariable() {
        String result = renderer.render("kb={{sys.kbId}}", context);
        assertEquals("kb=100", result);
    }

    @Test
    void render_inputsShortcut() {
        String result = renderer.render("Q: {{inputs.question}}", context);
        assertEquals("Q: 什么是 QuickBoot？", result);
    }

    @Test
    void resolveObject_missingNode_returnsNull() {
        assertNull(renderer.resolveObject("missing.field", context));
    }

    @Test
    void render_startNodeOutputField() {
        Map<String, Object> startOut = new HashMap<>();
        startOut.put("input", "hello-input");
        context.putNodeOutputs("start_1", startOut);
        String result = renderer.render("{{start_1.input}}", context);
        assertEquals("hello-input", result);
    }

    @Test
    void render_unknownPlaceholder_replacedWithEmpty() {
        String result = renderer.render("x={{unknown.field}}", context);
        assertEquals("x=", result);
    }
}
