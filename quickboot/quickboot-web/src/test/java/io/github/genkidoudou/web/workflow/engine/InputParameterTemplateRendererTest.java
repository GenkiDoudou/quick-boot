package io.github.genkidoudou.web.workflow.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link InputParameterTemplateRenderer} 单元测试。
 */
class InputParameterTemplateRendererTest {

    private InputParameterTemplateRenderer renderer;
    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        renderer = new InputParameterTemplateRenderer(new TemplateRenderer());
        context = new WorkflowContext(1L, "start_1");
        Map<String, Object> kbOut = new HashMap<>();
        kbOut.put("contextText", "QuickBoot 是企业级脚手架");
        context.putNodeOutputs("kb_1", kbOut);
        Map<String, Object> startOut = new HashMap<>();
        startOut.put("question", "什么是 QuickBoot？");
        context.putNodeOutputs("start_1", startOut);
    }

    @Test
    void resolveInputVariables_mapsUpstreamToLocalNames() {
        List<Map<String, String>> defs = List.of(
            Map.of("key", "context", "value", "{{kb_1.contextText}}"),
            Map.of("key", "question", "value", "{{start_1.question}}")
        );

        Map<String, Object> locals = renderer.resolveInputVariables(defs, context);

        assertEquals("QuickBoot 是企业级脚手架", locals.get("context"));
        assertEquals("什么是 QuickBoot？", locals.get("question"));
    }

    @Test
    void render_replacesInputParameterPlaceholdersInPrompt() {
        Map<String, Object> locals = Map.of(
            "context", "QuickBoot 是企业级脚手架",
            "question", "什么是 QuickBoot？"
        );

        String rendered = renderer.render("上下文：{{context}}\n问题：{{question}}", locals);

        assertEquals("上下文：QuickBoot 是企业级脚手架\n问题：什么是 QuickBoot？", rendered);
    }

    @Test
    void render_unknownPlaceholder_becomesEmpty() {
        Map<String, Object> locals = Map.of("question", "hello");

        String rendered = renderer.render("Q: {{question}} 上游: {{start_1.question}}", locals);

        assertEquals("Q: hello 上游: ", rendered);
    }

    @Test
    void collectReferencedRoots_extractsRootKeys() {
        Map<String, Object> roots = renderer.collectReferencedRoots(
            "请回答 {{question}}",
            "参考 {{context.title}}"
        );

        assertEquals(Boolean.TRUE, roots.get("question"));
        assertEquals(Boolean.TRUE, roots.get("context"));
        assertFalse(roots.containsKey("title"));
    }
}
