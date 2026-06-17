package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.InputParameterTemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.support.WorkflowCodeExecutor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码节点：解析输入参数后执行 JavaScript / Python 自定义逻辑，按输出结构返回结果。
 * <p>
 * 代码内通过 {@code params['参数名']} 读取输入；须定义 {@code main} 并 return 对象。
 */
@Component
public class CodeNodeHandler implements NodeHandler {

    private static final String ERROR_MODE_FALLBACK = "fallback";

    private final InputParameterTemplateRenderer inputParameterRenderer;
    private final WorkflowCodeExecutor codeExecutor;

    public CodeNodeHandler(InputParameterTemplateRenderer inputParameterRenderer,
                           WorkflowCodeExecutor codeExecutor) {
        this.inputParameterRenderer = inputParameterRenderer;
        this.codeExecutor = codeExecutor;
    }

    @Override
    public String type() {
        return WfNodeType.CODE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String language = data.get("language") == null ? "javascript" : String.valueOf(data.get("language"));
        String code = data.get("code") == null ? "" : String.valueOf(data.get("code"));
        long timeoutMs = resolveTimeoutMs(data.get("timeoutSec"));
        String errorMode = data.get("errorMode") == null ? "abort" : String.valueOf(data.get("errorMode"));

        Map<String, Object> params = inputParameterRenderer.resolveInputVariables(data.get("inputVariables"), context);
        Map<String, Object> traceInputs = new LinkedHashMap<>();
        traceInputs.put("language", language);
        traceInputs.put("params", params);
        traceInputs.put("timeoutSec", timeoutMs / 1000.0);

        try {
            Map<String, Object> raw = codeExecutor.execute(language, code, params, timeoutMs);
            Map<String, Object> outputs = projectOutputs(raw, data.get("outputVariables"));
            outputs.put("isSuccess", true);
            outputs.put("errorBody", "");
            return NodeResult.successWithTrace(outputs, traceInputs);
        } catch (Exception ex) {
            String message = ex.getMessage() == null ? "代码执行失败" : ex.getMessage();
            if (ERROR_MODE_FALLBACK.equalsIgnoreCase(errorMode)) {
                Map<String, Object> outputs = buildFallbackOutputs(data.get("fallbackOutputs"), message);
                outputs.put("isSuccess", false);
                outputs.put("errorBody", message);
                return NodeResult.successWithTrace(outputs, traceInputs);
            }
            return NodeResult.failed(message);
        }
    }

    private long resolveTimeoutMs(Object timeoutSecObj) {
        double sec = 60.0;
        if (timeoutSecObj instanceof Number number) {
            sec = number.doubleValue();
        } else if (timeoutSecObj != null) {
            try {
                sec = Double.parseDouble(String.valueOf(timeoutSecObj).trim());
            } catch (NumberFormatException ignored) {
                // keep default
            }
        }
        sec = Math.max(0.1, Math.min(sec, 60.0));
        return Math.round(sec * 1000);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> projectOutputs(Map<String, Object> raw, Object outputVariablesObj) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        if (!(outputVariablesObj instanceof List<?> list) || list.isEmpty()) {
            outputs.putAll(raw);
            return outputs;
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> row)) {
                continue;
            }
            String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();
            if (StrUtil.isBlank(key)) {
                continue;
            }
            outputs.put(key, raw.get(key));
        }
        return outputs;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildFallbackOutputs(Object fallbackObj, String errorMessage) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        if (fallbackObj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                outputs.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return outputs;
        }
        if (fallbackObj instanceof String str && StrUtil.isNotBlank(str)) {
            try {
                JSONObject json = JSONUtil.parseObj(str.trim());
                outputs.putAll(json);
                return outputs;
            } catch (Exception ignored) {
                // fall through
            }
        }
        outputs.put("result", "");
        outputs.put("_error", errorMessage);
        return outputs;
    }
}
