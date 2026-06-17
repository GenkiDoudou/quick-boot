package io.github.genkidoudou.web.workflow.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link JsonSerializeUtil} 单元测试。
 */
class JsonSerializeUtilTest {

    @Test
    void serialize_objectToCompactJson() {
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("name", "张三");
        assertEquals("{\"name\":\"张三\"}", JsonSerializeUtil.serialize(obj));
    }

    @Test
    void serialize_arrayToCompactJson() {
        assertEquals("[1,2,3]", JsonSerializeUtil.serialize(List.of(1, 2, 3)));
    }

    @Test
    void serialize_legalJsonString_passThrough() {
        assertEquals("{\"a\":1}", JsonSerializeUtil.serialize("{\"a\":1}"));
    }

    @Test
    void serialize_plainText_wrapsAsJsonString() {
        assertEquals("\"hello\"", JsonSerializeUtil.serialize("hello"));
    }

    @Test
    void serialize_nullOrEmpty_returnsEmptyString() {
        assertEquals("", JsonSerializeUtil.serialize(null));
        assertEquals("", JsonSerializeUtil.serialize(""));
        assertEquals("", JsonSerializeUtil.serialize("   "));
    }
}
