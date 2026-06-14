package io.github.genkidoudou.web.ai.prompt.support;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词模板变量校验：提取 {@code {{var}}} 根键名并与声明表比对。
 * <p>
 * 逻辑对齐工作流 {@code useUpstreamVariables.js} 的 {@code extractTemplateRootKeys}。
 */
public final class AiPromptVariableValidator {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)}}");

    private AiPromptVariableValidator() {
    }

    /**
     * 从多个模板文本中提取占位符根键名集合。
     *
     * @param templates 待扫描模板
     * @return 根键名集合（有序）
     */
    public static Set<String> extractRootKeys(String... templates) {
        Set<String> roots = new LinkedHashSet<>();
        if (templates == null) {
            return roots;
        }
        for (String template : templates) {
            if (StrUtil.isBlank(template)) {
                continue;
            }
            Matcher matcher = PLACEHOLDER.matcher(template);
            while (matcher.find()) {
                String root = extractRootKey(matcher.group(1).trim());
                if (StrUtil.isNotBlank(root)) {
                    roots.add(root);
                }
            }
        }
        return roots;
    }

    /**
     * 从内容段 Map 中提取全部占位符根键名。
     *
     * @param sections 内容段
     * @return 根键名集合
     */
    public static Set<String> extractRootKeysFromSections(Map<String, String> sections) {
        if (sections == null || sections.isEmpty()) {
            return Set.of();
        }
        return extractRootKeys(sections.values().toArray(new String[0]));
    }

    /**
     * 查找未在声明表中的占位符根键名。
     *
     * @param sections       内容段
     * @param declaredVarKeys 已声明 var_key 集合
     * @return 未声明变量列表；空表示全部已声明
     */
    public static List<String> findUndeclaredVars(Map<String, String> sections, Collection<String> declaredVarKeys) {
        Set<String> declared = normalizeDeclaredKeys(declaredVarKeys);
        List<String> undeclared = new ArrayList<>();
        for (String root : extractRootKeysFromSections(sections)) {
            if (!declared.contains(root)) {
                undeclared.add(root);
            }
        }
        return undeclared;
    }

    private static Set<String> normalizeDeclaredKeys(Collection<String> declaredVarKeys) {
        Set<String> declared = new LinkedHashSet<>();
        if (declaredVarKeys == null) {
            return declared;
        }
        for (String key : declaredVarKeys) {
            if (StrUtil.isNotBlank(key)) {
                declared.add(key.trim());
            }
        }
        return declared;
    }

    private static String extractRootKey(String expr) {
        if (StrUtil.isBlank(expr)) {
            return "";
        }
        String normalized = expr.trim().replaceAll("\\[(\\d+)]", ".$1");
        int dot = normalized.indexOf('.');
        return dot <= 0 ? normalized : normalized.substring(0, dot);
    }
}
