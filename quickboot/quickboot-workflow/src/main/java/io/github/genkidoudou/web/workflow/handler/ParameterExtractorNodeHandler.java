package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.support.WorkflowAiGuard;
import io.github.genkidoudou.web.workflow.support.WorkflowTokenUsageSupport;
import io.github.genkidoudou.web.workflow.support.WorkflowTraceMetaSupport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameter Extractor 节点：LLM + JSON Schema 约束的结构化参数抽取。
 */
@Component
public class ParameterExtractorNodeHandler implements NodeHandler {

    private final WorkflowAiGuard aiGuard;
    private final TemplateRenderer templateRenderer;

    public ParameterExtractorNodeHandler(WorkflowAiGuard aiGuard, TemplateRenderer templateRenderer) {
        this.aiGuard = aiGuard;
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.PARAMETER_EXTRACTOR;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        ChatModel chatModel = aiGuard.requireChatModelInstance(workflowId(context));
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String query = templateRenderer.render(String.valueOf(data.getOrDefault("query", "")), context);
        Object schema = data.get("schema");
        String schemaJson = schema == null ? "{}" : JSONUtil.toJsonStr(schema);
        String prompt = """
            从以下文本中抽取结构化参数，严格符合 JSON Schema 字段定义。
            Schema：%s
            文本：%s
            请仅返回 JSON 对象，不要包含其它说明。
            """.formatted(schemaJson, query);
        try {
            ChatClient client = ChatClient.builder(chatModel).build();
            WorkflowTokenUsageSupport.CallTextAndUsage callResult =
                WorkflowTokenUsageSupport.resolveCall(client.prompt().user(prompt).call());
            String json = callResult.text();
            JSONObject obj = JSONUtil.parseObj(extractJson(json));
            Map<String, Object> outputs = new HashMap<>(obj);
            Map<String, Object> traceInputs = new LinkedHashMap<>();
            traceInputs.put("query", truncate(query, 500));
            WorkflowTraceMetaSupport.enrichTraceInputs(
                traceInputs, "parameter-extractor", callResult.tokenUsage(), Map.of());
            return NodeResult.successWithTrace(outputs, traceInputs);
        } catch (Exception ex) {
            return NodeResult.failed("参数抽取失败: " + ex.getMessage());
        }
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

    private Long workflowId(WorkflowContext context) {
        Object id = context.getSysVariables().get("workflowId");
        if (id instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
