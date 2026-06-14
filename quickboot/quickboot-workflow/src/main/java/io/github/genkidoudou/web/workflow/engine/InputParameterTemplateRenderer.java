package io.github.genkidoudou.web.workflow.engine;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.workflow.util.JsonDeepParseUtil;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 大模型节点输入参数模板渲染：提示词中仅允许 {@code {{参数名}}} / {@code {{参数名.子字段}}} 引用
 * {@link #resolveInputVariables(Object, WorkflowContext)} 解析后的本地变量。
 */
@Component
public class InputParameterTemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)}}");
    private static final Pattern SINGLE_PLACEHOLDER = Pattern.compile("^\\{\\{([^}]+)}}\\s*$");
    private static final Pattern BRACKET_INDEX = Pattern.compile("\\[(\\d+)]");

    private final TemplateRenderer templateRenderer;

    public InputParameterTemplateRenderer(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    /**
     * 将节点配置的 inputVariables 解析为参数名 → 值 映射。
     *
     * @param inputVariablesObj 节点 data.inputVariables
     * @param context           工作流上下文
     * @return 有序映射，空配置时返回空 Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> resolveInputVariables(Object inputVariablesObj, WorkflowContext context) {
        Map<String, Object> locals = new LinkedHashMap<>();
        if (!(inputVariablesObj instanceof List<?> list)) {
            return locals;
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> row)) {
                continue;
            }
            String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();
            String valueTpl = row.get("value") == null ? "" : String.valueOf(row.get("value"));
            if (StrUtil.isBlank(key) || StrUtil.isBlank(valueTpl)) {
                continue;
            }
            locals.put(key, resolveInputValue(valueTpl, context));
        }
        return locals;
    }

    /**
     * 使用本地输入参数渲染模板；未声明的占位符替换为空串。
     *
     * @param template 含 {@code {{参数名}}} 的模板
     * @param locals   已解析的输入参数
     * @return 渲染结果
     */
    public String render(String template, Map<String, Object> locals) {
        if (StrUtil.isBlank(template)) {
            return template == null ? "" : template;
        }
        if (locals == null || locals.isEmpty()) {
            return stripUnresolvedPlaceholders(template);
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String expr = matcher.group(1).trim();
            Object value = resolveLocalExpression(expr, locals);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(stringify(value)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 收集模板中引用的输入参数根名（点号或方括号前的段）。
     *
     * @param templates 待扫描模板
     * @return 根名集合
     */
    public Map<String, Object> collectReferencedRoots(String... templates) {
        Map<String, Object> roots = new LinkedHashMap<>();
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
                    roots.put(root, Boolean.TRUE);
                }
            }
        }
        return roots;
    }

    private Object resolveInputValue(String valueTpl, WorkflowContext context) {
        Matcher matcher = SINGLE_PLACEHOLDER.matcher(valueTpl.trim());
        if (matcher.matches()) {
            Object resolved = templateRenderer.resolveObject(matcher.group(1).trim(), context);
            if (resolved != null) {
                return JsonDeepParseUtil.deepParse(resolved);
            }
            return "";
        }
        return templateRenderer.render(valueTpl, context);
    }

    private Object resolveLocalExpression(String expr, Map<String, Object> locals) {
        if (StrUtil.isBlank(expr)) {
            return null;
        }
        String normalized = normalizeLocalExpression(expr);
        int dot = normalized.indexOf('.');
        if (dot <= 0) {
            return locals.get(normalized);
        }
        String rootKey = normalized.substring(0, dot);
        Object root = locals.get(rootKey);
        if (root == null) {
            return null;
        }
        return resolvePath(root, normalized.substring(dot + 1));
    }

    private String normalizeLocalExpression(String expr) {
        return BRACKET_INDEX.matcher(expr.trim()).replaceAll(".$1");
    }

    private String extractRootKey(String expr) {
        if (StrUtil.isBlank(expr)) {
            return "";
        }
        String normalized = normalizeLocalExpression(expr);
        int dot = normalized.indexOf('.');
        return dot <= 0 ? normalized : normalized.substring(0, dot);
    }

    @SuppressWarnings("unchecked")
    private Object resolvePath(Object root, String fieldPath) {
        if (root == null || StrUtil.isBlank(fieldPath)) {
            return null;
        }
        String[] parts = fieldPath.split("\\.");
        Object current = root;
        for (String part : parts) {
            if (current == null || StrUtil.isBlank(part)) {
                return null;
            }
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else if (current instanceof List<?> list) {
                try {
                    int index = Integer.parseInt(part);
                    current = index >= 0 && index < list.size() ? list.get(index) : null;
                } catch (NumberFormatException ex) {
                    return null;
                }
            } else {
                return null;
            }
        }
        return current;
    }

    private String stripUnresolvedPlaceholders(String template) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String stringify(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String str) {
            return str;
        }
        if (value instanceof Map<?, ?> || value instanceof List<?>) {
            return JSONUtil.toJsonStr(value);
        }
        return String.valueOf(value);
    }
}
