package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.InputParameterTemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.util.JsonDeserializeUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 反序列化节点：将 JSON 字符串解析为对象，可选按 outputFields 提取字段，固定输出 {@code output}。
 */
@Component
public class JsonDeserializeNodeHandler implements NodeHandler {

    private final InputParameterTemplateRenderer inputParameterRenderer;

    public JsonDeserializeNodeHandler(InputParameterTemplateRenderer inputParameterRenderer) {
        this.inputParameterRenderer = inputParameterRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.JSON_DESERIALIZE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        Map<String, Object> locals = inputParameterRenderer.resolveInputVariables(data.get("inputVariables"), context);
        ResolvedInput resolved = resolveFirstInput(data.get("inputVariables"), locals);

        JsonDeserializeUtil.ParseOutcome parsed = JsonDeserializeUtil.resolveRoot(resolved.value());
        if (!parsed.success()) {
            return NodeResult.failed(parsed.message());
        }

        List<?> outputFields = data.get("outputFields") instanceof List<?> list ? list : List.of();
        JsonDeserializeUtil.BuildOutcome built = JsonDeserializeUtil.buildOutput(parsed.value(), outputFields);
        if (!built.success()) {
            return NodeResult.failed(built.message());
        }

        Map<String, Object> outputs = new HashMap<>();
        outputs.put("output", built.output());

        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("inputPreview", truncatePreview(resolved.value()));
        trace.put("outputKeys", extractOutputKeys(built.output()));
        trace.put("fieldCount", countConfiguredFields(outputFields));

        return NodeResult.successWithTrace(outputs, trace);
    }

    @SuppressWarnings("unchecked")
    private ResolvedInput resolveFirstInput(Object inputVariablesObj, Map<String, Object> locals) {
        if (!(inputVariablesObj instanceof List<?> list)) {
            return ResolvedInput.empty();
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> row)) {
                continue;
            }
            String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();
            String valueTpl = row.get("value") == null ? "" : String.valueOf(row.get("value")).trim();
            if (StrUtil.isBlank(key) || StrUtil.isBlank(valueTpl)) {
                continue;
            }
            return new ResolvedInput(key, locals.get(key));
        }
        return ResolvedInput.empty();
    }

    private String truncatePreview(Object value) {
        if (value == null) {
            return "";
        }
        String text = value instanceof String str ? str : JSONUtil.toJsonStr(value);
        return text.length() <= 500 ? text : text.substring(0, 500) + "…";
    }

    @SuppressWarnings("unchecked")
    private List<String> extractOutputKeys(Object output) {
        if (!(output instanceof Map<?, ?> map)) {
            return List.of();
        }
        return new ArrayList<>(map.keySet().stream().map(String::valueOf).toList());
    }

    private int countConfiguredFields(List<?> outputFields) {
        if (outputFields == null || outputFields.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Object item : outputFields) {
            if (!(item instanceof Map<?, ?> row)) {
                continue;
            }
            String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();
            if (!key.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private record ResolvedInput(String key, Object value) {
        static ResolvedInput empty() {
            return new ResolvedInput("", null);
        }
    }
}
