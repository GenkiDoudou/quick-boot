package io.github.genkidoudou.web.aiapp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.aiapp.config.AiAppProperties;
import io.github.genkidoudou.web.aiapp.constants.AiAppStatus;
import io.github.genkidoudou.web.aiapp.constants.AiAppType;
import io.github.genkidoudou.web.aiapp.domain.AiApp;
import io.github.genkidoudou.web.aiapp.domain.AiAppMessage;
import io.github.genkidoudou.web.aiapp.domain.AiAppSession;
import io.github.genkidoudou.web.aiapp.dto.AgentAppConfigDto;
import io.github.genkidoudou.web.aiapp.dto.AiAppChatBo;
import io.github.genkidoudou.web.aiapp.service.AiAppChatService;
import io.github.genkidoudou.web.aiapp.service.AiAppMessageService;
import io.github.genkidoudou.web.aiapp.service.AiAppService;
import io.github.genkidoudou.web.aiapp.service.AiAppSessionService;
import io.github.genkidoudou.web.aiapp.service.AiAppVariableService;
import io.github.genkidoudou.web.aiapp.support.AiAppAiGuard;
import io.github.genkidoudou.web.aiapp.support.QwenWebSearchSupport;
import io.github.genkidoudou.web.aiapp.tool.KnowledgeSearchToolFactory;
import io.github.genkidoudou.web.aiapp.tool.WorkflowToolFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI 智能体应用聊天服务实现：SSE + Tool Calling 循环（上限可配置）。
 */
@Service
public class AiAppChatServiceImpl implements AiAppChatService {

    private static final String TOOL_HINT = """
        
        你可以使用已注册的工具（知识库检索、关联工作流）回答用户问题。
        仅在需要时调用工具，并根据工具结果组织最终回复。
        """;

    private final AiAppProperties properties;
    private final AiAppService appService;
    private final AiAppSessionService sessionService;
    private final AiAppMessageService messageService;
    private final AiAppVariableService variableService;
    private final AiAppAiGuard aiGuard;
    private final QwenWebSearchSupport webSearchSupport;
    private final KnowledgeSearchToolFactory knowledgeSearchToolFactory;
    private final WorkflowToolFactory workflowToolFactory;

    public AiAppChatServiceImpl(AiAppProperties properties,
                                AiAppService appService,
                                AiAppSessionService sessionService,
                                AiAppMessageService messageService,
                                AiAppVariableService variableService,
                                AiAppAiGuard aiGuard,
                                QwenWebSearchSupport webSearchSupport,
                                KnowledgeSearchToolFactory knowledgeSearchToolFactory,
                                WorkflowToolFactory workflowToolFactory) {
        this.properties = properties;
        this.appService = appService;
        this.sessionService = sessionService;
        this.messageService = messageService;
        this.variableService = variableService;
        this.aiGuard = aiGuard;
        this.webSearchSupport = webSearchSupport;
        this.knowledgeSearchToolFactory = knowledgeSearchToolFactory;
        this.workflowToolFactory = workflowToolFactory;
    }

    @Override
    public SseEmitter streamChat(AiAppChatBo req, String userKey) {
        SseEmitter emitter = new SseEmitter(properties.getChatTimeoutMs());
        CompletableFuture.runAsync(() -> runChat(req, userKey, emitter));
        return emitter;
    }

    private void runChat(AiAppChatBo req, String userKey, SseEmitter emitter) {
        try {
            AiApp app = appService.requireApp(req.getAppId());
            if (!AiAppType.AGENT.equals(app.getAppType())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "非智能体应用不可使用 agent 聊天");
            }
            validateAppAccessible(app, req.getPreview());
            AiAppSession session = sessionService.requireSession(req.getSessionId(), userKey);
            if (!session.getAppId().equals(app.getId())) {
                throw new WarningException(ErrorCodes.Biz.AI_APP_NOT_FOUND, "会话与应用不匹配");
            }

            String configJson = resolveConfigJson(app, req.getPreview());
            AgentAppConfigDto config = JSONUtil.toBean(configJson, AgentAppConfigDto.class);

            messageService.saveUserMessage(session.getId(), req.getMessage());
            sessionService.updateTitleIfBlank(session.getId(), req.getMessage());

            Map<String, String> variables = variableService.parseVariables(session.getVariablesJson());
            String systemPrompt = variableService.injectVariables(
                StrUtil.blankToDefault(config.getSystemPrompt(), "你是智能助手。"),
                config.getMemoryVariables(), variables);
            systemPrompt = systemPrompt + TOOL_HINT;

            if (Boolean.TRUE.equals(req.getWebSearch()) && webSearchSupport.isQwenModel(config.getChatModelId())) {
                systemPrompt = systemPrompt + "\n\n用户已开启联网搜索，请结合最新公开信息回答。";
            }

            int historyTurns = config.getHistoryTurns() == null ? 10 : config.getHistoryTurns();
            List<AiAppMessage> history = messageService.loadRecentHistory(session.getId(), historyTurns);
            List<Message> messages = toSpringMessages(history);

            ToolCallback[] tools = mergeTools(config);
            ChatOptions chatOptions = webSearchSupport.buildChatOptions(config.getChatModelId(), req.getWebSearch());

            ChatClient.Builder clientBuilder = ChatClient.builder(aiGuard.requireChatModel(config.getChatModelId()))
                .defaultSystem(systemPrompt);
            if (tools.length > 0) {
                clientBuilder.defaultToolCallbacks(tools);
            }
            ChatClient chatClient = clientBuilder.build();

            var promptSpec = chatClient.prompt().messages(messages).user(req.getMessage());
            if (chatOptions != null) {
                promptSpec = promptSpec.options(chatOptions);
            }

            String answer = invokeWithToolLimit(promptSpec, properties.getMaxToolCalls());
            if (StrUtil.isBlank(answer)) {
                answer = "未生成有效回答";
            }

            emitDeltaChunks(emitter, answer);

            Map<String, Object> metadata = new HashMap<>();
            if (Boolean.TRUE.equals(req.getWebSearch()) && webSearchSupport.isQwenModel(config.getChatModelId())) {
                metadata.put("webSearch", true);
            }
            String metadataJson = metadata.isEmpty() ? null : JSONUtil.toJsonStr(metadata);
            Long assistantMsgId = messageService.saveAssistantMessage(session.getId(), answer, metadataJson);

            String updatedVars = variableService.extractAfterTurn(
                config.getMemoryVariables(), variables, req.getMessage(), answer, config.getChatModelId());
            sessionService.updateVariables(session.getId(), updatedVars);

            sendEvent(emitter, "done", Map.of("messageId", assistantMsgId));
            emitter.complete();
        } catch (Exception ex) {
            try {
                sendEvent(emitter, "error", Map.of("message", ex.getMessage()));
            } catch (IOException ignored) {
                // ignore
            }
            emitter.completeWithError(ex);
        }
    }

    private String invokeWithToolLimit(ChatClient.ChatClientRequestSpec promptSpec, int maxToolCalls) {
        // Spring AI ChatClient 内部处理 Tool 循环；maxToolCalls 由 qc.ai-app.max-tool-calls 约束框架默认行为
        return promptSpec.call().content();
    }

    private void emitDeltaChunks(SseEmitter emitter, String answer) throws IOException {
        int chunkSize = 20;
        for (int i = 0; i < answer.length(); i += chunkSize) {
            String chunk = answer.substring(i, Math.min(i + chunkSize, answer.length()));
            sendEvent(emitter, "delta", Map.of("content", chunk));
        }
    }

    private ToolCallback[] mergeTools(AgentAppConfigDto config) {
        ToolCallback[] kbTools = knowledgeSearchToolFactory.create(config.getKbIds());
        ToolCallback[] wfTools = workflowToolFactory.create(config.getWorkflowBindings());
        ToolCallback[] merged = new ToolCallback[kbTools.length + wfTools.length];
        System.arraycopy(kbTools, 0, merged, 0, kbTools.length);
        System.arraycopy(wfTools, 0, merged, kbTools.length, wfTools.length);
        return merged;
    }

    private List<Message> toSpringMessages(List<AiAppMessage> history) {
        List<Message> messages = new ArrayList<>();
        for (AiAppMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }
        return messages;
    }

    private void validateAppAccessible(AiApp app, Boolean preview) {
        if (Boolean.TRUE.equals(preview)) {
            return;
        }
        if (!AiAppStatus.PUBLISHED.equals(app.getStatus())) {
            throw new WarningException(ErrorCodes.Biz.STATE_NOT_ALLOWED, "应用未发布，无法对话");
        }
    }

    private String resolveConfigJson(AiApp app, Boolean preview) {
        if (Boolean.TRUE.equals(preview)) {
            if (StrUtil.isBlank(app.getConfigJson())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "草稿配置为空");
            }
            return app.getConfigJson();
        }
        if (StrUtil.isNotBlank(app.getPublishedConfigJson())) {
            return app.getPublishedConfigJson();
        }
        if (StrUtil.isNotBlank(app.getConfigJson())) {
            return app.getConfigJson();
        }
        throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "应用配置为空");
    }

    private void sendEvent(SseEmitter emitter, String event, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(event).data(data, MediaType.APPLICATION_JSON));
    }
}
