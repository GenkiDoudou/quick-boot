package io.github.genkidoudou.web.workflow.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JsonDeserializeUtil} 单元测试。
 */
class JsonDeserializeUtilTest {

    @Test
    void parse_wholeObject_withoutFields() {
        JsonDeserializeUtil.ParseOutcome outcome = JsonDeserializeUtil.parse("{\"name\":\"张三\"}");
        assertTrue(outcome.success());
        assertEquals(Map.of("name", "张三"), outcome.value());
    }

    @Test
    void extractByPath_nestedThreeLevels() {
        Map<String, Object> root = Map.of(
            "data", Map.of(
                "user", Map.of("name", "张三")
            )
        );
        assertEquals("张三", JsonDeserializeUtil.extractByPath(root, "data.user.name"));
    }

    @Test
    void buildOutput_withFields_extractsKeys() {
        Map<String, Object> root = Map.of(
            "data", Map.of(
                "user", Map.of("name", "张三", "age", 18)
            )
        );
        List<Map<String, String>> fields = List.of(
            Map.of("key", "name", "path", "data.user.name", "type", "string"),
            Map.of("key", "age", "path", "data.user.age", "type", "number")
        );
        JsonDeserializeUtil.BuildOutcome built = JsonDeserializeUtil.buildOutput(root, fields);
        assertTrue(built.success());
        assertEquals("张三", ((Map<?, ?>) built.output()).get("name"));
        assertEquals(18, ((Map<?, ?>) built.output()).get("age"));
    }

    @Test
    void parse_depthGreaterThanThree_fails() {
        String json = "{\"a\":{\"b\":{\"c\":{\"d\":1}}}}";
        JsonDeserializeUtil.ParseOutcome outcome = JsonDeserializeUtil.parse(json);
        assertFalse(outcome.success());
        assertTrue(outcome.message().contains("深度"));
    }

    @Test
    void parse_invalidJson_fails() {
        JsonDeserializeUtil.ParseOutcome outcome = JsonDeserializeUtil.parse("{bad}");
        assertFalse(outcome.success());
        assertTrue(outcome.message().contains("解析"));
    }

    @Test
    void parse_emptyInput_fails() {
        JsonDeserializeUtil.ParseOutcome outcome = JsonDeserializeUtil.parse("   ");
        assertFalse(outcome.success());
        assertTrue(outcome.message().contains("空"));
    }

    @Test
    void buildOutput_withFields_rootArray_fails() {
        JsonDeserializeUtil.BuildOutcome built = JsonDeserializeUtil.buildOutput(
            List.of(1, 2),
            List.of(Map.of("key", "x", "path", "x"))
        );
        assertFalse(built.success());
    }

    @Test
    void extractByPath_missingPath_returnsNull() {
        Map<String, Object> root = Map.of("name", "张三");
        assertNull(JsonDeserializeUtil.extractByPath(root, "missing.path"));
    }

    @Test
    void buildOutput_emptyFields_returnsRoot() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("items", List.of(1, 2));
        JsonDeserializeUtil.BuildOutcome built = JsonDeserializeUtil.buildOutput(root, List.of());
        assertTrue(built.success());
        assertEquals(root, built.output());
    }
}
