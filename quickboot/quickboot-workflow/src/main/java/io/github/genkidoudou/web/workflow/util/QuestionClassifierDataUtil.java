package io.github.genkidoudou.web.workflow.util;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 意图识别（question-classifier）节点 data 归一化与校验工具。
 * <p>
 * 兼容旧版 {@code classes[]} 结构，统一为 {@code intents[]}；运行时 ID 为数组下标 + 1。
 */
public final class QuestionClassifierDataUtil {

    public static final String FALLBACK_HANDLE = "0";
    public static final int MAX_INTENTS = 50;

    private QuestionClassifierDataUtil() {
    }

    /**
     * 归一化节点 data：{@code classes} → {@code intents}，迁移输入/输出字段。
     *
     * @param raw 原始 data，可为 null
     * @return 可变副本，含 intents 等字段
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> normalize(Map<String, Object> raw) {
        Map<String, Object> data = raw == null ? new LinkedHashMap<>() : new LinkedHashMap<>(raw);
        if (data.get("intents") instanceof List<?> intents && !intents.isEmpty()) {
            data.put("intents", normalizeIntentList((List<Map<String, Object>>) intents));
        } else if (data.get("classes") instanceof List<?> classes) {
            data.put("intents", migrateClasses((List<Map<String, Object>>) classes));
            data.remove("classes");
        } else {
            data.put("intents", List.of());
        }
        if (!data.containsKey("systemPrompt")) {
            data.put("systemPrompt", "");
        }
        migrateChatModelId(data);
        data.remove("mode");
        data.remove("modelId");
        migrateInputVariables(data);
        return data;
    }

    /**
     * 校验归一化后的 data，失败时抛出 {@link IllegalArgumentException}。
     *
     * @param nodeId 节点 ID，用于错误信息
     * @param data   已归一化的 data
     */
    @SuppressWarnings("unchecked")
    public static void validate(String nodeId, Map<String, Object> data) {
        Map<String, Object> normalized = normalize(data);
        if (!hasValidInput(normalized)) {
            throw new IllegalArgumentException("意图识别节点 " + nodeId + " 须配置输入参数（参数名与上游取值）");
        }
        List<Map<String, Object>> intents = (List<Map<String, Object>>) normalized.get("intents");
        if (intents == null || intents.isEmpty()) {
            throw new IllegalArgumentException("意图识别节点 " + nodeId + " 至少需要一个意图");
        }
        if (intents.size() > MAX_INTENTS) {
            throw new IllegalArgumentException("意图识别节点 " + nodeId + " 意图数量超限（最多 " + MAX_INTENTS + " 个）");
        }
        for (int i = 0; i < intents.size(); i++) {
            Map<String, Object> intent = intents.get(i);
            String name = intent.get("name") == null ? "" : String.valueOf(intent.get("name")).trim();
            if (StrUtil.isBlank(name)) {
                throw new IllegalArgumentException("意图识别节点 " + nodeId + " 第 " + (i + 1) + " 个意图名称不能为空");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean hasValidInput(Map<String, Object> data) {
        if (data.get("inputVariables") instanceof List<?> list) {
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> row)) {
                    continue;
                }
                String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();
                String value = row.get("value") == null ? "" : String.valueOf(row.get("value")).trim();
                if (StrUtil.isNotBlank(key) && StrUtil.isNotBlank(value)) {
                    return true;
                }
            }
        }
        return StrUtil.isNotBlank(String.valueOf(data.getOrDefault("query", "")).trim());
    }

    /**
     * 返回意图数量（归一化后）。
     */
    @SuppressWarnings("unchecked")
    public static int intentCount(Map<String, Object> data) {
        Map<String, Object> normalized = normalize(data);
        List<?> intents = (List<?>) normalized.get("intents");
        return intents == null ? 0 : intents.size();
    }

    /**
     * 将旧字符串 classId handle 映射为数字 handle（1..N），无法映射则返回 null。
     */
    @SuppressWarnings("unchecked")
    public static String mapLegacyHandle(String legacyHandle, List<Map<String, Object>> classes) {
        if (legacyHandle == null || legacyHandle.isBlank()) {
            return null;
        }
        if (FALLBACK_HANDLE.equals(legacyHandle) || "default".equals(legacyHandle)) {
            return FALLBACK_HANDLE;
        }
        if (classes != null) {
            for (int i = 0; i < classes.size(); i++) {
                Object id = classes.get(i).get("id");
                if (id != null && legacyHandle.equals(String.valueOf(id))) {
                    return String.valueOf(i + 1);
                }
            }
        }
        try {
            int n = Integer.parseInt(legacyHandle);
            if (n >= 0) {
                return legacyHandle;
            }
        } catch (NumberFormatException ignored) {
            // 非数字
        }
        return null;
    }

    private static void migrateChatModelId(Map<String, Object> data) {
        if (data.containsKey("chatModelId")) {
            return;
        }
        Object legacy = data.get("modelId");
        data.put("chatModelId", legacy);
    }

    @SuppressWarnings("unchecked")
    private static void migrateInputVariables(Map<String, Object> data) {
        if (data.get("inputVariables") instanceof List<?> list && !list.isEmpty()) {
            syncQueryFromInputVariables(data);
            return;
        }
        String legacyQuery = String.valueOf(data.getOrDefault("query", "")).trim();
        List<Map<String, Object>> inputVariables = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("key", "query");
        row.put("value", StrUtil.isBlank(legacyQuery) ? "{{start_1.question}}" : legacyQuery);
        inputVariables.add(row);
        data.put("inputVariables", inputVariables);
        syncQueryFromInputVariables(data);
    }

    @SuppressWarnings("unchecked")
    private static void syncQueryFromInputVariables(Map<String, Object> data) {
        if (!(data.get("inputVariables") instanceof List<?> list) || list.isEmpty()) {
            return;
        }
        Object first = list.get(0);
        if (!(first instanceof Map<?, ?> row)) {
            return;
        }
        String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();
        if (StrUtil.isNotBlank(key)) {
            data.put("query", "{{" + key + "}}");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> migrateClasses(List<Map<String, Object>> classes) {
        List<Map<String, Object>> intents = new ArrayList<>();
        if (classes == null) {
            return intents;
        }
        for (Map<String, Object> cls : classes) {
            if (cls == null) {
                continue;
            }
            Map<String, Object> intent = new LinkedHashMap<>();
            intent.put("name", cls.get("name") == null ? "" : String.valueOf(cls.get("name")).trim());
            intent.put("examples", splitExamples(cls.get("description")));
            intents.add(intent);
        }
        return intents;
    }

    private static List<Map<String, Object>> normalizeIntentList(List<Map<String, Object>> intents) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : intents) {
            if (item == null) {
                continue;
            }
            Map<String, Object> intent = new LinkedHashMap<>();
            intent.put("name", item.get("name") == null ? "" : String.valueOf(item.get("name")).trim());
            Object examples = item.get("examples");
            if (examples instanceof List<?> list) {
                List<String> ex = new ArrayList<>();
                for (Object e : list) {
                    if (e != null && !String.valueOf(e).isBlank()) {
                        ex.add(String.valueOf(e).trim());
                    }
                }
                intent.put("examples", ex);
            } else {
                intent.put("examples", List.of());
            }
            result.add(intent);
        }
        return result;
    }

    private static List<String> splitExamples(Object description) {
        if (description == null) {
            return List.of();
        }
        String text = String.valueOf(description).trim();
        if (text.isEmpty()) {
            return List.of();
        }
        String[] lines = text.split("\\r?\\n");
        List<String> examples = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                examples.add(trimmed);
            }
        }
        return examples.isEmpty() ? List.of(text) : examples;
    }
}
