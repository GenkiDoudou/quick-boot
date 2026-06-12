package io.github.genkidoudou.web.workflow.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 递归将 Map/List 中「看起来像 JSON 的字符串」解析为嵌套结构。
 */
public final class JsonDeepParseUtil {

    private JsonDeepParseUtil() {
    }

    /**
     * 深度解析对象/数组/JSON 字符串。
     *
     * @param value 原始值
     * @return 解析后的值
     */
    public static Object deepParse(Object value) {
        return deepParseJsonStringValue(value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepParseJsonStrings(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            result.put(String.valueOf(entry.getKey()), deepParseJsonStringValue(entry.getValue()));
        }
        return result;
    }

    private static List<Object> deepParseJsonStrings(List<?> source) {
        List<Object> result = new ArrayList<>(source.size());
        for (Object item : source) {
            result.add(deepParseJsonStringValue(item));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object deepParseJsonStringValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return deepParseJsonStrings(map);
        }
        if (value instanceof List<?> list) {
            return deepParseJsonStrings(list);
        }
        if (value instanceof String str) {
            String trimmed = str.trim();
            if (JSONUtil.isTypeJSONObject(trimmed)) {
                return deepParseJsonStrings(JSONUtil.parseObj(trimmed));
            }
            if (JSONUtil.isTypeJSONArray(trimmed)) {
                return deepParseJsonStrings(JSONUtil.parseArray(trimmed));
            }
            return str;
        }
        return value;
    }
}
