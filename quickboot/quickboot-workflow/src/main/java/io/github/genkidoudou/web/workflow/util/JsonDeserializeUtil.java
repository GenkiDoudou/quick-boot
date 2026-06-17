package io.github.genkidoudou.web.workflow.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 反序列化节点：解析 JSON 文本、嵌套深度校验、点路径字段提取。
 */
public final class JsonDeserializeUtil {

    /** 允许的最大 JSON 树嵌套深度（从根起算）。 */
    public static final int MAX_DEPTH = 3;

    private JsonDeserializeUtil() {
    }

    /**
     * 将运行时输入解析为 JSON 根值（支持 JSON 文本或已解析的 Map/List）。
     *
     * @param value 解析后的输入值
     * @return 成功时携带根值；失败时携带错误信息
     */
    public static ParseOutcome resolveRoot(Object value) {
        if (value == null) {
            return ParseOutcome.fail("输入为空");
        }
        if (value instanceof String str) {
            return parse(str);
        }
        if (value instanceof Map<?, ?> || value instanceof List<?>) {
            Object plain = toPlain(value);
            int depth = computeDepth(plain);
            if (depth > MAX_DEPTH) {
                return ParseOutcome.fail("JSON 嵌套深度超过 " + MAX_DEPTH + " 层");
            }
            return ParseOutcome.ok(plain);
        }
        return parse(String.valueOf(value));
    }

    /**
     * 解析 JSON 文本为运行时值。
     *
     * @param text JSON 文本
     * @return 成功时携带根值；失败时携带错误信息
     */
    public static ParseOutcome parse(String text) {
        if (StrUtil.isBlank(text)) {
            return ParseOutcome.fail("输入为空");
        }
        String trimmed = text.trim();
        try {
            Object root = JSONUtil.parse(trimmed);
            int depth = computeDepth(root);
            if (depth > MAX_DEPTH) {
                return ParseOutcome.fail("JSON 嵌套深度超过 " + MAX_DEPTH + " 层");
            }
            return ParseOutcome.ok(toPlain(root));
        } catch (Exception ex) {
            return ParseOutcome.fail("JSON 解析失败: " + ex.getMessage());
        }
    }

    /**
     * 按 outputFields 从根值组装输出对象。
     *
     * @param root         解析后的根值
     * @param outputFields 字段配置（key、path、type）
     * @return 成功时携带 output；失败时携带错误信息
     */
    @SuppressWarnings("unchecked")
    public static BuildOutcome buildOutput(Object root, List<?> outputFields) {
        if (outputFields == null || outputFields.isEmpty()) {
            return BuildOutcome.ok(root);
        }
        if (root instanceof List<?>) {
            return BuildOutcome.fail("配置了输出字段时，JSON 根须为对象");
        }
        if (!(root instanceof Map<?, ?>)) {
            return BuildOutcome.fail("配置了输出字段时，JSON 根须为对象");
        }
        Map<String, Object> output = new LinkedHashMap<>();
        for (Object item : outputFields) {
            if (!(item instanceof Map<?, ?> row)) {
                continue;
            }
            String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();
            if (key.isEmpty()) {
                continue;
            }
            String path = row.get("path") == null ? "" : String.valueOf(row.get("path")).trim();
            if (path.isEmpty()) {
                path = key;
            }
            output.put(key, extractByPath(root, path));
        }
        return BuildOutcome.ok(output);
    }

    /**
     * 计算 JSON 值树的最大嵌套深度。
     *
     * @param value 根值
     * @return 深度（primitive 根为 0，空 object 为 1）
     */
    public static int computeDepth(Object value) {
        if (value == null || isPrimitive(value)) {
            return 0;
        }
        if (value instanceof Map<?, ?> map) {
            int maxChild = 0;
            for (Object child : map.values()) {
                maxChild = Math.max(maxChild, computeDepth(child));
            }
            return maxChild + 1;
        }
        if (value instanceof List<?> list) {
            int maxChild = 0;
            for (Object child : list) {
                maxChild = Math.max(maxChild, computeDepth(child));
            }
            return maxChild + 1;
        }
        return 0;
    }

    /**
     * 按点路径从根 Map 提取字段值；中间节点非 object 或路径不存在时返回 null。
     *
     * @param root 根值
     * @param path 点路径，如 {@code data.user.name}
     * @return 提取值或 null
     */
    @SuppressWarnings("unchecked")
    public static Object extractByPath(Object root, String path) {
        if (root == null || StrUtil.isBlank(path)) {
            return null;
        }
        Object current = root;
        for (String segment : path.split("\\.")) {
            if (segment.isEmpty()) {
                continue;
            }
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(segment);
            if (current == null) {
                return null;
            }
        }
        return toPlain(current);
    }

    /**
     * 将 Hutool JSON 类型转为普通 Map/List，便于下游引用。
     *
     * @param value 原始值
     * @return 普通 Java 类型
     */
    public static Object toPlain(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof JSONObject jsonObject) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (String key : jsonObject.keySet()) {
                map.put(key, toPlain(jsonObject.get(key)));
            }
            return map;
        }
        if (value instanceof JSONArray jsonArray) {
            List<Object> list = new ArrayList<>();
            for (Object item : jsonArray) {
                list.add(toPlain(item));
            }
            return list;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> plain = new LinkedHashMap<>();
            map.forEach((k, v) -> plain.put(String.valueOf(k), toPlain(v)));
            return plain;
        }
        if (value instanceof List<?> list) {
            List<Object> plain = new ArrayList<>();
            for (Object item : list) {
                plain.add(toPlain(item));
            }
            return plain;
        }
        return value;
    }

    private static boolean isPrimitive(Object value) {
        return value instanceof String
            || value instanceof Number
            || value instanceof Boolean
            || value instanceof Character;
    }

    /**
     * JSON 解析结果。
     *
     * @param success 是否成功
     * @param value   根值
     * @param message 失败信息
     */
    public record ParseOutcome(boolean success, Object value, String message) {
        public static ParseOutcome ok(Object value) {
            return new ParseOutcome(true, value, null);
        }

        public static ParseOutcome fail(String message) {
            return new ParseOutcome(false, null, message);
        }
    }

    /**
     * output 组装结果。
     *
     * @param success 是否成功
     * @param output  输出对象
     * @param message 失败信息
     */
    public record BuildOutcome(boolean success, Object output, String message) {
        public static BuildOutcome ok(Object output) {
            return new BuildOutcome(true, output, null);
        }

        public static BuildOutcome fail(String message) {
            return new BuildOutcome(false, null, message);
        }
    }
}
