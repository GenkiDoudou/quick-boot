package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.support.WorkflowAiGuard;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Question Classifier 节点：LLM + JSON 输出进行意图分类，返回 classId/className 供分支路由。
 */
@Component
public class QuestionClassifierNodeHandler implements NodeHandler {

    private final WorkflowAiGuard aiGuard;
    private final TemplateRenderer templateRenderer;

    public QuestionClassifierNodeHandler(WorkflowAiGuard aiGuard, TemplateRenderer templateRenderer) {
        this.aiGuard = aiGuard;
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.QUESTION_CLASSIFIER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        ChatModel chatModel = aiGuard.requireChatModelInstance(workflowId(context));
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String query = templateRenderer.render(String.valueOf(data.getOrDefault("query", "")), context);
        List<Map<String, Object>> classes = data.get("classes") instanceof List<?> list
            ? (List<Map<String, Object>>) list : List.of();
        String classDesc = classes.stream()
            .map(c -> c.get("id") + ":" + c.get("name"))
            .collect(Collectors.joining(", "));
        String prompt = """
            请对以下问题进行意图分类，仅从给定类别中选择最合适的一个。
            类别列表：%s
            问题：%s
            请仅返回 JSON：{"classId":"...","className":"..."}
            """.formatted(classDesc, query);
        try {
            ChatClient client = ChatClient.builder(chatModel).build();
            String json = client.prompt().user(prompt).call().content();
            JSONObject obj = JSONUtil.parseObj(extractJson(json));
            String classId = obj.getStr("classId");
            String className = obj.getStr("className");
            if (StrUtil.isBlank(classId)) {
                Object fallback = data.get("fallbackClassId");
                classId = fallback == null ? "default" : String.valueOf(fallback);
            }
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("classId", classId);
            outputs.put("className", className);
            return NodeResult.successWithBranch(outputs, classId);
        } catch (Exception ex) {
            return NodeResult.failed("意图分类失败: " + ex.getMessage());
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

    private Long workflowId(WorkflowContext context) {
        Object id = context.getSysVariables().get("workflowId");
        if (id instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
