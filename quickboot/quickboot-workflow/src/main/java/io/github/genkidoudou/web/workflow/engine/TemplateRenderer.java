package io.github.genkidoudou.web.workflow.engine;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工作流模板渲染器：解析 {@code {{nodeId.field}}}、{@code {{sys.*}}}、{@code {{inputs.key}}} 占位符。
 * <p>
 * 禁止使用 SpEL 或脚本引擎，仅做安全的路径取值与字符串替换。
 */
@Component
public class TemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)}}");

    /**
     * 渲染模板字符串，将占位符替换为上下文中的值。
     *
     * @param template 含占位符的模板
     * @param context  工作流运行时上下文
     * @return 渲染后的字符串；占位符无法解析时保留原样或替换为空串
     */
    public String render(String template, WorkflowContext context) {
        if (StrUtil.isBlank(template)) {
            return template;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String expr = matcher.group(1).trim();
            Object value = resolve(expr, context);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(stringify(value)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 解析表达式并返回值对象（非字符串化）。
     *
     * @param expr    表达式，如 {@code start_1.question}、{@code sys.kbId}、{@code inputs.question}
     * @param context 运行时上下文
     * @return 解析结果，无法解析时返回 null
     */
    public Object resolveObject(String expr, WorkflowContext context) {
        if (StrUtil.isBlank(expr)) {
            return null;
        }
        return resolve(expr.trim(), context);
    }

    private Object resolve(String expr, WorkflowContext context) {
        if (expr.startsWith("sys.")) {
            String key = expr.substring(4);
            return context.getSysVariables().get(key);
        }
        if (expr.startsWith("inputs.")) {
            String key = expr.substring(7);
            return context.getRunInputs().get(key);
        }
        int dot = expr.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        String nodeId = expr.substring(0, dot);
        String fieldPath = expr.substring(dot + 1);
        Map<String, Object> nodeOutput = context.getNodeOutputs().get(nodeId);
        if (nodeOutput == null) {
            return null;
        }
        return resolvePath(nodeOutput, fieldPath);
    }

    @SuppressWarnings("unchecked")
    private Object resolvePath(Object root, String fieldPath) {
        if (root == null || StrUtil.isBlank(fieldPath)) {
            return null;
        }
        String[] parts = fieldPath.split("\\.");
        Object current = root;
        for (String part : parts) {
            if (current == null) {
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
