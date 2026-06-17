package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.InputParameterTemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.support.WorkflowAiGuard;
import io.github.genkidoudou.web.workflow.util.QuestionClassifierDataUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 意图识别节点（question-classifier）：LLM + JSON 输出 index/reason，数字 handle 分支路由。
 */
@Component
public class QuestionClassifierNodeHandler implements NodeHandler {

    private final WorkflowAiGuard aiGuard;
    private final TemplateRenderer templateRenderer;
    private final InputParameterTemplateRenderer inputParameterRenderer;

    public QuestionClassifierNodeHandler(WorkflowAiGuard aiGuard,
                                         TemplateRenderer templateRenderer,
                                         InputParameterTemplateRenderer inputParameterRenderer) {
        this.aiGuard = aiGuard;
        this.templateRenderer = templateRenderer;
        this.inputParameterRenderer = inputParameterRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.QUESTION_CLASSIFIER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = QuestionClassifierDataUtil.normalize(
            node.getData() == null ? Map.of() : node.getData());

        Map<String, Object> locals = inputParameterRenderer.resolveInputVariables(data.get("inputVariables"), context);
        String query = resolveQueryText(data, context, locals);
        String systemPrompt = renderWithInputLocals(
            String.valueOf(data.getOrDefault("systemPrompt", "")).trim(), locals, context);
        List<Map<String, Object>> intents = (List<Map<String, Object>>) data.get("intents");
        Long chatModelId = parseLong(data.get("chatModelId"));

        Map<String, Object> traceInputs = buildTraceInputs(chatModelId, query, intents);

        ChatModel chatModel;
        try {
            chatModel = aiGuard.requireChatModelInstance(workflowId(context), chatModelId);
        } catch (Exception ex) {
            return NodeResult.failed("意图识别失败: 未配置可用 Chat 模型 - " + ex.getMessage());
        }

        String prompt = buildPrompt(intents, query, systemPrompt);
        try {
            ChatClient client = ChatClient.builder(chatModel).build();
            String json = client.prompt().user(prompt).call().content();
            return buildSuccessResult(parseModelResponse(json, intents), traceInputs);
        } catch (Exception ex) {
            return buildFallbackResult("模型调用失败: " + ex.getMessage(), traceInputs);
        }
    }

    private String resolveQueryText(Map<String, Object> data, WorkflowContext context, Map<String, Object> locals) {
        String queryExpr = String.valueOf(data.getOrDefault("query", "")).trim();
        if (!locals.isEmpty() && StrUtil.isNotBlank(queryExpr)) {
            return inputParameterRenderer.render(queryExpr, locals);
        }
        if (StrUtil.isNotBlank(queryExpr)) {
            return templateRenderer.render(queryExpr, context);
        }
        if (!locals.isEmpty()) {
            return String.valueOf(locals.values().iterator().next());
        }
        return "";
    }

    private String renderWithInputLocals(String template, Map<String, Object> locals, WorkflowContext context) {
        if (StrUtil.isBlank(template)) {
            return "";
        }
        if (!locals.isEmpty()) {
            return inputParameterRenderer.render(template, locals);
        }
        return templateRenderer.render(template, context);
    }

    private Map<String, Object> buildTraceInputs(Long chatModelId, String query,
                                                 List<Map<String, Object>> intents) {
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("chatModelId", chatModelId);
        trace.put("query", truncate(query, 500));
        trace.put("intentCount", intents == null ? 0 : intents.size());
        return trace;
    }

    private String buildPrompt(List<Map<String, Object>> intents, String query, String systemPrompt) {
        StringBuilder intentTable = new StringBuilder();
        for (int i = 0; i < intents.size(); i++) {
            Map<String, Object> intent = intents.get(i);
            int id = i + 1;
            intentTable.append(id).append(". ").append(intent.get("name"));
            Object examples = intent.get("examples");
            if (examples instanceof List<?> list && !list.isEmpty()) {
                intentTable.append("（示例：").append(String.join("；", list.stream()
                    .map(String::valueOf).toList())).append("）");
            }
            intentTable.append("\n");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("""
            你是意图识别助手。请根据用户输入，从下列意图序号中选择最匹配的一个。
            若无法匹配任何意图，返回 index 为 0。
            请仅返回 JSON，格式：{"index": 数字, "reason": "简要说明依据"}

            可选意图：
            """);
        sb.append(intentTable);
        if (StrUtil.isNotBlank(systemPrompt)) {
            sb.append("\n补充说明：\n").append(systemPrompt).append("\n");
        }
        sb.append("\n用户输入：\n").append(query);
        return sb.toString();
    }

    private ClassificationOutcome parseModelResponse(String raw, List<Map<String, Object>> intents) {
        try {
            JSONObject obj = JSONUtil.parseObj(extractJson(raw));
            Object idObj = obj.get("index");
            if (idObj == null) {
                idObj = obj.get("classificationId");
            }
            int index = 0;
            if (idObj instanceof Number number) {
                index = number.intValue();
            } else if (idObj != null) {
                index = Integer.parseInt(String.valueOf(idObj).trim());
            }
            String reason = obj.getStr("reason", "");
            if (index < 0 || index > intents.size()) {
                return ClassificationOutcome.fallback("无效意图序号: " + index, reason);
            }
            return new ClassificationOutcome(index, reason, false);
        } catch (Exception ex) {
            return ClassificationOutcome.fallback("解析失败: " + ex.getMessage(), "");
        }
    }

    private NodeResult buildSuccessResult(ClassificationOutcome outcome, Map<String, Object> traceInputs) {
        if (outcome.fallback) {
            return buildFallbackResult(outcome.reason, traceInputs);
        }
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("index", outcome.index);
        outputs.put("reason", outcome.reason == null ? "" : outcome.reason);
        return NodeResult.successWithBranchAndTrace(outputs, traceInputs, String.valueOf(outcome.index));
    }

    private NodeResult buildFallbackResult(String reason, Map<String, Object> traceInputs) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("index", 0);
        outputs.put("reason", reason == null ? "" : reason);
        return NodeResult.successWithBranchAndTrace(outputs, traceInputs, QuestionClassifierDataUtil.FALLBACK_HANDLE);
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long workflowId(WorkflowContext context) {
        Object id = context.getSysVariables().get("workflowId");
        if (id instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private record ClassificationOutcome(int index, String reason, boolean fallback) {
        static ClassificationOutcome fallback(String prefix, String modelReason) {
            String msg = prefix;
            if (StrUtil.isNotBlank(modelReason)) {
                msg = msg + "；" + modelReason;
            }
            return new ClassificationOutcome(0, msg, true);
        }
    }
}
