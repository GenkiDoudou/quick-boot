package io.github.genkidoudou.web.workflow.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.util.List;
import java.util.Map;

/**
 * JSON 序列化节点值转换：合法 JSON 文本透传，结构化值紧凑序列化。
 */
public final class JsonSerializeUtil {

    private JsonSerializeUtil() {
    }

    /**
     * 将运行时值序列化为 JSON 字符串。
     *
     * @param value 解析后的输入值，可为 null
     * @return 序列化结果；null/空/不可序列化时返回空串
     */
    public static String serialize(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String str) {
            String trimmed = str.trim();
            if (trimmed.isEmpty()) {
                return "";
            }
            if (isLegalJsonText(trimmed)) {
                return trimmed;
            }
            return JSONUtil.quote(str);
        }
        if (value instanceof Map<?, ?> || value instanceof List<?> || value instanceof Number || value instanceof Boolean) {
            return JSONUtil.toJsonStr(value);
        }
        try {
            return JSONUtil.toJsonStr(value);
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * 判断字符串是否为可透传的合法 JSON 文本。
     *
     * @param text 已 trim 的文本
     * @return 是否为合法 JSON
     */
    public static boolean isLegalJsonText(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        if (JSONUtil.isTypeJSON(text)) {
            return true;
        }
        try {
            Object parsed = JSONUtil.parse(text);
            if (parsed instanceof String) {
                return text.trim().startsWith("\"");
            }
            return parsed instanceof Number || parsed instanceof Boolean || parsed == null;
        } catch (Exception ex) {
            return false;
        }
    }
}
