package io.github.genkidoudou.web.ai.prompt.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.ai.prompt.constants.AiPromptType;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按 {@code promptType} 的必填段校验、A/B prompt 拼接与样例变量一层替换。
 */
public final class AiPromptSectionSupport {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)}}");

    private AiPromptSectionSupport() {
    }

    /**
     * 获取指定类型的已知段键（不含 CUSTOM 动态段）。
     *
     * @param promptType 场景类型
     * @return 段键集合
     */
    public static Set<String> knownSectionKeys(String promptType) {
        String type = AiPromptType.normalize(promptType);
        Set<String> keys = new LinkedHashSet<>();
        switch (type) {
            case AiPromptType.LLM -> {
                keys.add("systemPrompt");
                keys.add("userPrompt");
            }
            case AiPromptType.RAG -> {
                keys.add("systemPrompt");
                keys.add("userPromptTemplate");
            }
            case AiPromptType.CLASSIFIER -> {
                keys.add("systemPrompt");
                keys.add("instruction");
                keys.add("categoriesHint");
            }
            case AiPromptType.EXTRACTOR -> {
                keys.add("systemPrompt");
                keys.add("instruction");
                keys.add("outputSchemaHint");
            }
            default -> {
            }
        }
        return keys;
    }

    /**
     * 获取指定类型的必填段键。
     *
     * @param promptType 场景类型
     * @return 必填段键集合
     */
    public static Set<String> requiredSectionKeys(String promptType) {
        String type = AiPromptType.normalize(promptType);
        Set<String> keys = new LinkedHashSet<>();
        switch (type) {
            case AiPromptType.LLM -> keys.add("userPrompt");
            case AiPromptType.RAG -> keys.add("systemPrompt");
            case AiPromptType.CLASSIFIER, AiPromptType.EXTRACTOR -> keys.add("instruction");
            default -> {
            }
        }
        return keys;
    }

    /**
     * 校验必填段是否非空；CUSTOM 类型要求至少一段非空。
     *
     * @param promptType 场景类型
     * @param sections   内容段
     * @return 缺失的必填段键；空表示通过
     */
    public static List<String> findMissingRequiredSections(String promptType, Map<String, String> sections) {
        String type = AiPromptType.normalize(promptType);
        Map<String, String> safeSections = sections == null ? Map.of() : sections;

        if (AiPromptType.CUSTOM.equals(type)) {
            boolean anyNonBlank = safeSections.values().stream().anyMatch(StrUtil::isNotBlank);
            return anyNonBlank ? List.of() : List.of("(至少一段非空)");
        }

        List<String> missing = new java.util.ArrayList<>();
        for (String key : requiredSectionKeys(type)) {
            if (StrUtil.isBlank(safeSections.get(key))) {
                missing.add(key);
            }
        }
        return missing;
    }

    /**
     * 对内容段执行一层 {@code {{key}}} 样例变量替换。
     *
     * @param sections    原始内容段
     * @param sampleInput 样例变量键值
     * @return 替换后的内容段副本
     */
    public static Map<String, String> replaceSampleInput(Map<String, String> sections, Map<String, Object> sampleInput) {
        Map<String, String> replaced = new LinkedHashMap<>();
        if (sections == null) {
            return replaced;
        }
        Map<String, String> sampleStr = toStringMap(sampleInput);
        for (Map.Entry<String, String> entry : sections.entrySet()) {
            replaced.put(entry.getKey(), replaceOneLevel(entry.getValue(), sampleStr));
        }
        return replaced;
    }

    /**
     * 按 promptType 规则拼接 A/B 审计用完整 prompt 文本。
     *
     * @param promptType 场景类型
     * @param sections   已替换样例变量的内容段
     * @return 拼接后的 prompt
     */
    public static String renderPromptForAb(String promptType, Map<String, String> sections) {
        Map<String, String> safe = sections == null ? Map.of() : sections;
        String type = AiPromptType.normalize(promptType);
        return switch (type) {
            case AiPromptType.LLM -> joinParts(
                header("System", safe.get("systemPrompt")),
                header("User", safe.get("userPrompt"))
            );
            case AiPromptType.RAG -> joinParts(
                header("System", safe.get("systemPrompt")),
                header("UserTemplate", safe.get("userPromptTemplate"))
            );
            case AiPromptType.CLASSIFIER -> joinParts(
                header("System", safe.get("systemPrompt")),
                header("Instruction", safe.get("instruction")),
                header("CategoriesHint", safe.get("categoriesHint"))
            );
            case AiPromptType.EXTRACTOR -> joinParts(
                header("System", safe.get("systemPrompt")),
                header("Instruction", safe.get("instruction")),
                header("OutputSchemaHint", safe.get("outputSchemaHint"))
            );
            default -> joinAllSections(safe);
        };
    }

    /**
     * 解析 A/B Chat 调用的 system 与 user 消息（按 promptType）。
     *
     * @param promptType 场景类型
     * @param sections   已替换样例变量的内容段
     * @return [system, user]
     */
    public static String[] resolveChatMessages(String promptType, Map<String, String> sections) {
        Map<String, String> safe = sections == null ? Map.of() : sections;
        String type = AiPromptType.normalize(promptType);
        return switch (type) {
            case AiPromptType.LLM -> new String[]{
                nullToEmpty(safe.get("systemPrompt")),
                nullToEmpty(safe.get("userPrompt"))
            };
            case AiPromptType.RAG -> new String[]{
                nullToEmpty(safe.get("systemPrompt")),
                nullToEmpty(safe.get("userPromptTemplate"))
            };
            case AiPromptType.CLASSIFIER -> new String[]{
                nullToEmpty(safe.get("systemPrompt")),
                joinParts(
                    nullToEmpty(safe.get("instruction")),
                    nullToEmpty(safe.get("categoriesHint"))
                )
            };
            case AiPromptType.EXTRACTOR -> new String[]{
                nullToEmpty(safe.get("systemPrompt")),
                joinParts(
                    nullToEmpty(safe.get("instruction")),
                    nullToEmpty(safe.get("outputSchemaHint"))
                )
            };
            default -> new String[]{ "", joinAllSections(safe) };
        };
    }

    private static String joinAllSections(Map<String, String> sections) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sections.entrySet()) {
            if (StrUtil.isNotBlank(entry.getValue())) {
                if (!sb.isEmpty()) {
                    sb.append("\n\n");
                }
                sb.append('[').append(entry.getKey()).append("]\n").append(entry.getValue());
            }
        }
        return sb.toString();
    }

    private static String header(String label, String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        return "[" + label + "]\n" + content;
    }

    private static String joinParts(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (StrUtil.isBlank(part)) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append("\n\n");
            }
            sb.append(part);
        }
        return sb.toString();
    }

    private static String replaceOneLevel(String template, Map<String, String> sampleInput) {
        if (StrUtil.isBlank(template)) {
            return template == null ? "" : template;
        }
        if (sampleInput == null || sampleInput.isEmpty()) {
            return template;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String expr = matcher.group(1).trim();
            String root = extractRootKey(expr);
            String replacement = sampleInput.getOrDefault(root, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String extractRootKey(String expr) {
        if (StrUtil.isBlank(expr)) {
            return "";
        }
        String normalized = expr.trim().replaceAll("\\[(\\d+)]", ".$1");
        int dot = normalized.indexOf('.');
        return dot <= 0 ? normalized : normalized.substring(0, dot);
    }

    private static Map<String, String> toStringMap(Map<String, Object> sampleInput) {
        Map<String, String> map = new LinkedHashMap<>();
        if (sampleInput == null) {
            return map;
        }
        for (Map.Entry<String, Object> entry : sampleInput.entrySet()) {
            if (entry.getKey() != null) {
                map.put(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
            }
        }
        return map;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
