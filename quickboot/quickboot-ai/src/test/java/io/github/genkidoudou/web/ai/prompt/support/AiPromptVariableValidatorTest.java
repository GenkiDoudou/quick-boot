package io.github.genkidoudou.web.ai.prompt.support;

import io.github.genkidoudou.web.ai.prompt.constants.AiPromptType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AiPromptVariableValidator} 与 {@link AiPromptSectionSupport} 单元测试。
 */
class AiPromptVariableValidatorTest {

    @Test
    void extractRootKeysFromNestedPlaceholder() {
        Set<String> roots = AiPromptVariableValidator.extractRootKeys(
            "Hello {{question.sub}} and {{name[0]}}");
        assertIterableEquals(List.of("question", "name"), roots);
    }

    @Test
    void findUndeclaredVarsBlocksUnknownKeys() {
        Map<String, String> sections = Map.of("userPrompt", "Answer: {{question}} about {{topic}}");
        List<String> undeclared = AiPromptVariableValidator.findUndeclaredVars(sections, List.of("question"));
        assertEquals(List.of("topic"), undeclared);
    }

    @Test
    void findUndeclaredVarsPassesWhenDeclared() {
        Map<String, String> sections = Map.of("userPrompt", "{{question}}");
        assertTrue(AiPromptVariableValidator.findUndeclaredVars(sections, List.of("question")).isEmpty());
    }

    @Test
    void requiredSectionsForLlm() {
        Map<String, String> sections = Map.of("systemPrompt", "sys");
        List<String> missing = AiPromptSectionSupport.findMissingRequiredSections(AiPromptType.LLM, sections);
        assertEquals(List.of("userPrompt"), missing);
    }

    @Test
    void requiredSectionsForCustomNeedsNonBlank() {
        List<String> missing = AiPromptSectionSupport.findMissingRequiredSections(AiPromptType.CUSTOM, Map.of());
        assertEquals(List.of("(至少一段非空)"), missing);
    }

    @Test
    void replaceSampleInputOneLevel() {
        Map<String, String> sections = Map.of("userPrompt", "请回答：{{question}}");
        Map<String, Object> sample = Map.of("question", "什么是 QuickBoot");
        Map<String, String> rendered = AiPromptSectionSupport.replaceSampleInput(sections, sample);
        assertEquals("请回答：什么是 QuickBoot", rendered.get("userPrompt"));
    }

    @Test
    void replaceSampleInputKeepsUnknownWhenNoSample() {
        Map<String, String> sections = Map.of("userPrompt", "{{unknown}}");
        Map<String, String> rendered = AiPromptSectionSupport.replaceSampleInput(sections, Map.of());
        assertEquals("{{unknown}}", rendered.get("userPrompt"));
    }

    @Test
    void renderPromptForAbLlm() {
        Map<String, String> sections = new LinkedHashMap<>();
        sections.put("systemPrompt", "You are helpful");
        sections.put("userPrompt", "Hi");
        String prompt = AiPromptSectionSupport.renderPromptForAb(AiPromptType.LLM, sections);
        assertTrue(prompt.contains("[System]"));
        assertTrue(prompt.contains("[User]"));
        assertTrue(prompt.contains("You are helpful"));
    }
}
