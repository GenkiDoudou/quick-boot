package io.github.genkidoudou.web.workflow.engine;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.stream.WorkflowStreamEmitter;
import io.github.genkidoudou.web.workflow.util.JsonDeepParseUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 输出/结束节点共用：按 {@code outputMode} 解析返回变量或返回文本。
 */
@Component
public class OutputNodeExecutor {

    private static final String MODE_TEXT = "text";
    private static final String MODE_VARIABLES = "variables";

    private static final Pattern SINGLE_PLACEHOLDER = Pattern.compile("^\\{\\{([^}]+)}}\\s*$");

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)}}");

    private final TemplateRenderer templateRenderer;
    private final WorkflowStreamEmitter streamEmitter;

    public OutputNodeExecutor(TemplateRenderer templateRenderer, WorkflowStreamEmitter streamEmitter) {
        this.templateRenderer = templateRenderer;
        this.streamEmitter = streamEmitter;
    }

    /**
     * 执行输出配置，生成节点 outputs。
     *
     * @param node    图节点
     * @param context 运行时上下文
     * @return 输出字段
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String outputMode = resolveOutputMode(data);
        if (MODE_TEXT.equals(outputMode)) {
            return executeTextMode(node, data, context);
        }
        return executeVariablesMode(data, context);
    }

    private Map<String, Object> executeTextMode(GraphNodeDto node, Map<String, Object> data, WorkflowContext context) {
        Map<String, Object> outputs = new HashMap<>();
        String outputTemplate = data.get("output") == null ? "" : String.valueOf(data.get("output"));
        if (StrUtil.isNotBlank(outputTemplate)) {
            String rendered = templateRenderer.render(outputTemplate, context);
            outputs.put("text", rendered);
            boolean streaming = Boolean.TRUE.equals(data.get("streaming")) && context.isStreamEnabled();
            if (streaming && StrUtil.isNotBlank(rendered)) {
                streamEmitter.emitLlmDelta(context.getRunId(), node.getId(), rendered, rendered);
            }
        }
        if (data.containsKey("citations") && data.get("citations") != null) {
            String citationsTpl = String.valueOf(data.get("citations"));
            if (StrUtil.isNotBlank(citationsTpl)) {
                outputs.put("citations", templateRenderer.render(citationsTpl, context));
            }
        }
        mergeOutputVariables(data, context, outputs);
        return outputs;
    }

    private Map<String, Object> executeVariablesMode(Map<String, Object> data, WorkflowContext context) {
        Map<String, Object> outputs = new HashMap<>();
        mergeOutputVariables(data, context, outputs);
        return outputs;
    }

    @SuppressWarnings("unchecked")
    private void mergeOutputVariables(Map<String, Object> data, WorkflowContext context, Map<String, Object> outputs) {
        Object outputVariablesObj = data.get("outputVariables");
        if (!(outputVariablesObj instanceof List<?> list)) {
            return;
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
            outputs.put(key, resolveOutputValue(valueTpl, context));
        }
    }

    private String resolveOutputMode(Map<String, Object> data) {
        Object mode = data.get("outputMode");
        if (MODE_TEXT.equals(mode)) {
            return MODE_TEXT;
        }
        if (MODE_VARIABLES.equals(mode)) {
            return MODE_VARIABLES;
        }
        String outputTemplate = data.get("output") == null ? "" : String.valueOf(data.get("output"));
        if (StrUtil.isNotBlank(outputTemplate)) {
            return MODE_TEXT;
        }
        return MODE_VARIABLES;
    }

    private Object resolveOutputValue(String valueTpl, WorkflowContext context) {
        if (StrUtil.isBlank(valueTpl)) {
            return "";
        }
        String trimmed = valueTpl.trim();
        Matcher single = SINGLE_PLACEHOLDER.matcher(trimmed);
        if (single.matches()) {
            Object resolved = templateRenderer.resolveObject(single.group(1).trim(), context);
            if (resolved != null) {
                return JsonDeepParseUtil.deepParse(resolved);
            }
            return "";
        }
        List<String> expressions = new ArrayList<>();
        Matcher multi = PLACEHOLDER.matcher(trimmed);
        while (multi.find()) {
            expressions.add(multi.group(1).trim());
        }
        if (expressions.size() > 1 && isPlaceholderOnlyTemplate(trimmed, expressions)) {
            List<Object> resolvedList = new ArrayList<>();
            for (String expr : expressions) {
                resolvedList.add(templateRenderer.resolveObject(expr, context));
            }
            if (allResolvedEqual(resolvedList)) {
                Object first = resolvedList.get(0);
                return first == null ? "" : JsonDeepParseUtil.deepParse(first);
            }
        }
        return templateRenderer.render(valueTpl, context);
    }

    private boolean isPlaceholderOnlyTemplate(String trimmed, List<String> expressions) {
        String rebuilt = String.join("", expressions.stream().map(e -> "{{" + e + "}}").toList());
        return trimmed.replaceAll("\\s+", "").equals(rebuilt.replaceAll("\\s+", ""));
    }

    private boolean allResolvedEqual(List<Object> values) {
        if (values.isEmpty()) {
            return true;
        }
        Object first = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            if (!objectsDeepEqual(first, values.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean objectsDeepEqual(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a instanceof List<?> listA && b instanceof List<?> listB) {
            return listA.equals(listB);
        }
        if (a instanceof Map<?, ?> mapA && b instanceof Map<?, ?> mapB) {
            return mapA.equals(mapB);
        }
        return Objects.equals(a, b);
    }
}
