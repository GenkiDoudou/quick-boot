package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.InputParameterTemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.util.JsonSerializeUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 序列化节点：将单个输入参数值转为紧凑 JSON 字符串，固定输出 {@code output}。
 */
@Component
public class JsonSerializeNodeHandler implements NodeHandler {

    private final InputParameterTemplateRenderer inputParameterRenderer;

    public JsonSerializeNodeHandler(InputParameterTemplateRenderer inputParameterRenderer) {
        this.inputParameterRenderer = inputParameterRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.JSON_SERIALIZE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        Map<String, Object> locals = inputParameterRenderer.resolveInputVariables(data.get("inputVariables"), context);
        ResolvedInput resolved = resolveFirstInput(data.get("inputVariables"), locals);

        String output = JsonSerializeUtil.serialize(resolved.value());
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("output", output);

        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("inputKey", resolved.key());
        trace.put("inputPreview", truncatePreview(resolved.value()));

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
        String text = String.valueOf(value);
        return text.length() <= 500 ? text : text.substring(0, 500) + "…";
    }

    private record ResolvedInput(String key, Object value) {
        static ResolvedInput empty() {
            return new ResolvedInput("", null);
        }
    }
}
