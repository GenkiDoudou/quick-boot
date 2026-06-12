package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.stream.WorkflowStreamEmitter;
import io.github.genkidoudou.web.workflow.support.WorkflowAiGuard;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM 节点：调用 Spring AI ChatModel；{@code streaming=true} 时向 SSE 推送 delta。
 */
@Component
public class LlmNodeHandler implements NodeHandler {

    private final WorkflowAiGuard aiGuard;
    private final TemplateRenderer templateRenderer;
    private final WorkflowStreamEmitter streamEmitter;

    public LlmNodeHandler(WorkflowAiGuard aiGuard,
                          TemplateRenderer templateRenderer,
                          WorkflowStreamEmitter streamEmitter) {
        this.aiGuard = aiGuard;
        this.templateRenderer = templateRenderer;
        this.streamEmitter = streamEmitter;
    }

    @Override
    public String type() {
        return WfNodeType.LLM;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        ChatModel chatModel = aiGuard.requireChatModelInstance(workflowId(context));
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String systemPrompt = render(data.get("systemPrompt"), context);
        String userPrompt = render(data.get("userPrompt"), context);
        boolean streaming = Boolean.TRUE.equals(data.get("streaming")) && context.isStreamEnabled();

        try {
            if (streaming) {
                return executeStreaming(node.getId(), context, chatModel, systemPrompt, userPrompt);
            }
            ChatClient client = ChatClient.builder(chatModel).defaultSystem(systemPrompt).build();
            String text = client.prompt().user(userPrompt).call().content();
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("text", text);
            return NodeResult.success(outputs);
        } catch (Exception ex) {
            return NodeResult.failed("LLM 调用失败: " + ex.getMessage());
        }
    }

    private NodeResult executeStreaming(String nodeId, WorkflowContext context, ChatModel chatModel,
                                        String systemPrompt, String userPrompt) {
        Prompt prompt = new Prompt(userPrompt);
        Flux<ChatResponse> flux = chatModel.stream(prompt);
        StringBuilder accumulated = new StringBuilder();
        flux.doOnNext(response -> {
            if (response.getResult() != null && response.getResult().getOutput() != null) {
                String delta = response.getResult().getOutput().getText();
                if (delta != null && !delta.isEmpty()) {
                    accumulated.append(delta);
                    streamEmitter.emitLlmDelta(context.getRunId(), nodeId, delta, accumulated.toString());
                }
            }
        }).blockLast();
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("text", accumulated.toString());
        return NodeResult.success(outputs);
    }

    private String render(Object template, WorkflowContext context) {
        return template == null ? "" : templateRenderer.render(String.valueOf(template), context);
    }

    private Long workflowId(WorkflowContext context) {
        Object id = context.getSysVariables().get("workflowId");
        if (id instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
