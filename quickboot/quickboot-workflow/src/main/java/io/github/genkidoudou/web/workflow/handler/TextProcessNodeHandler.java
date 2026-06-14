package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.InputParameterTemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文本处理节点：先解析 inputVariables 映射上游值，再在模板中通过 {@code {{参数名}}} 引用；
 * 支持字符串拼接（数组元素默认逗号连接）与按分隔符拆分为 items 数组。
 */
@Component
public class TextProcessNodeHandler implements NodeHandler {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)}}");
    private static final Pattern BRACKET_INDEX = Pattern.compile("\\[(\\d+)]");

    private final TemplateRenderer templateRenderer;
    private final InputParameterTemplateRenderer inputParameterRenderer;

    public TextProcessNodeHandler(TemplateRenderer templateRenderer,
                                  InputParameterTemplateRenderer inputParameterRenderer) {
        this.templateRenderer = templateRenderer;
        this.inputParameterRenderer = inputParameterRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.TEXT_PROCESS;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String mode = data.get("processMode") == null ? "join" : String.valueOf(data.get("processMode"));
        Map<String, Object> locals = inputParameterRenderer.resolveInputVariables(data.get("inputVariables"), context);
        Map<String, Object> outputs = new HashMap<>();

        if ("split".equalsIgnoreCase(mode)) {
            String sourceTpl = data.get("source") == null ? "" : String.valueOf(data.get("source"));
            String delimiter = data.get("delimiter") == null ? "" : String.valueOf(data.get("delimiter"));
            String sourceText = renderWithLocals(sourceTpl, locals, context, false);
            List<String> items = splitByDelimiter(sourceText, delimiter);
            outputs.put("items", items);
            outputs.put("count", items.size());
            outputs.put("text", sourceText);
        } else {
            String template = data.get("template") == null ? "" : String.valueOf(data.get("template"));
            String output = renderWithLocals(template, locals, context, true);
            outputs.put("output", output);
            outputs.put("text", output);
            outputs.put("result", output);
        }
        return NodeResult.success(outputs);
    }

    /**
     * 优先使用 inputVariables 本地参数渲染；无本地参数时回退为全局上游模板渲染（兼容旧图）。
     */
    private String renderWithLocals(String template, Map<String, Object> locals,
                                    WorkflowContext context, boolean joinArraysWithComma) {
        if (StrUtil.isBlank(template)) {
            return template == null ? "" : template;
        }
        if (locals != null && !locals.isEmpty()) {
            return joinArraysWithComma
                ? renderJoinFromLocals(template, locals)
                : inputParameterRenderer.render(template, locals);
        }
        return joinArraysWithComma
            ? renderLegacyJoinTemplate(template, context)
            : templateRenderer.render(template, context);
    }

    private String renderJoinFromLocals(String template, Map<String, Object> locals) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String expr = normalizeBracketPath(matcher.group(1).trim());
            Object value = resolveLocalExpression(expr, locals);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(stringifyForJoin(value)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String renderLegacyJoinTemplate(String template, WorkflowContext context) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String expr = normalizeBracketPath(matcher.group(1).trim());
            Object value = templateRenderer.resolveObject(expr, context);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(stringifyForJoin(value)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Object resolveLocalExpression(String expr, Map<String, Object> locals) {
        if (StrUtil.isBlank(expr)) {
            return null;
        }
        String normalized = normalizeBracketPath(expr);
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

    private String normalizeBracketPath(String expr) {
        if (StrUtil.isBlank(expr)) {
            return expr;
        }
        return BRACKET_INDEX.matcher(expr).replaceAll(".$1");
    }

    private String stringifyForJoin(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String str) {
            return str;
        }
        if (value instanceof List<?> list) {
            return list.stream()
                .map(this::stringifyForJoin)
                .collect(Collectors.joining(","));
        }
        return String.valueOf(value);
    }

    private List<String> splitByDelimiter(String text, String delimiter) {
        List<String> items = new ArrayList<>();
        if (text == null) {
            return items;
        }
        if (delimiter == null || delimiter.isEmpty()) {
            items.add(text);
            return items;
        }
        int from = 0;
        int idx = text.indexOf(delimiter, from);
        while (idx >= 0) {
            items.add(text.substring(from, idx));
            from = idx + delimiter.length();
            idx = text.indexOf(delimiter, from);
        }
        items.add(text.substring(from));
        return items;
    }
}
