package io.github.genkidoudou.web.aiapp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.SaTokenAsyncRunner;
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

    private static final String TOOL_HINT_BASE = """
        
        工具返回的是参考结果，你必须用自己的话归纳后回答用户，禁止原样粘贴大段原文。
        若工具执行失败，请如实转述失败原因，不要编造「知识库不可用」等借口。
        """;

    private static final String KB_CONTEXT_USER_PREFIX = """
        请严格根据【知识库检索上下文】回答【用户问题】。
        要求：语言自然连贯；可概括要点但勿大段复制原文。
        若上下文不足以回答问题，必须明确回复「未找到相关内容」，禁止使用模型常识或外部知识。
        
        """;

    /** 仅绑定知识库、检索无命中时的固定回复（不调用大模型，避免用常识作答）。 */
    private static final String KB_MISS_REPLY = "未找到相关内容";

    private static final String KB_MISS_USER_PREFIX = """
        【知识库检索结果】未在绑定的知识库中找到与用户问题相关的片段。
        若你有工作流工具可处理该问题，请调用工作流；否则请明确告知用户未找到相关内容。
        禁止使用模型预训练知识或常识作答。
        
        【用户问题】
        """;

    /** 仅绑定工作流、未绑知识库时，引导模型优先调用 Tool 而非用常识作答。 */
    private static final String WF_ONLY_USER_PREFIX = """
        请通过已绑定的工作流工具处理【用户问题】，再根据工具返回结果用自然语言回答。
        禁止跳过工作流工具、直接使用模型预训练知识或常识作答。
        
        【用户问题】
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
        String token = SaTokenAsyncRunner.captureTokenValue();
        CompletableFuture.runAsync(() -> SaTokenAsyncRunner.run(token, () -> runChat(req, userKey, emitter)));
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

            String configJson = resolveConfigJson(app, req.getPreview(), req.getPreviewConfigJson());
            AgentAppConfigDto config = parseAgentConfig(configJson);

            messageService.saveUserMessage(session.getId(), req.getMessage());
            sessionService.updateTitleIfBlank(session.getId(), req.getMessage());

            String kbOnlyMiss = resolveKbOnlyMissAnswer(req.getMessage(), config);
            if (kbOnlyMiss != null) {
                emitDeltaChunks(emitter, kbOnlyMiss);
                Long assistantMsgId = messageService.saveAssistantMessage(session.getId(), kbOnlyMiss, null);
                sendEvent(emitter, "done", Map.of("messageId", assistantMsgId));
                emitter.complete();
                return;
            }

            Map<String, String> variables = variableService.parseVariables(session.getVariablesJson());
            String systemPrompt = variableService.injectVariables(
                StrUtil.blankToDefault(config.getSystemPrompt(), "你是智能助手。"),
                config.getMemoryVariables(), variables);
            systemPrompt = systemPrompt + buildToolHint(config);

            if (Boolean.TRUE.equals(req.getWebSearch()) && webSearchSupport.isQwenModel(config.getChatModelId())) {
                systemPrompt = systemPrompt + "\n\n用户已开启联网搜索，请结合最新公开信息回答。";
            }

            int historyTurns = config.getHistoryTurns() == null ? 10 : config.getHistoryTurns();
            List<AiAppMessage> history = messageService.loadRecentHistory(session.getId(), historyTurns);
            List<Message> messages = toSpringMessages(history);

            String userMessage = buildUserMessage(req.getMessage(), config);

            ToolCallback[] tools = mergeTools(config, Boolean.TRUE.equals(req.getPreview()));
            ChatOptions chatOptions = webSearchSupport.buildChatOptions(config.getChatModelId(), req.getWebSearch());

            ChatClient.Builder clientBuilder = ChatClient.builder(aiGuard.requireChatModel(config.getChatModelId()))
                .defaultSystem(systemPrompt);
            if (tools.length > 0) {
                clientBuilder.defaultToolCallbacks(tools);
            }
            ChatClient chatClient = clientBuilder.build();

            var promptSpec = chatClient.prompt().messages(messages).user(userMessage);
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

    /**
     * 仅绑定知识库且无检索命中时，直接返回固定文案，避免大模型用常识作答。
     *
     * @return 固定回复；若需继续走大模型则返回 {@code null}
     */
    private String resolveKbOnlyMissAnswer(String rawMessage, AgentAppConfigDto config) {
        if (config.getKbIds() == null || config.getKbIds().isEmpty()) {
            return null;
        }
        if (hasWorkflowBindings(config)) {
            return null;
        }
        if (!isKbSearchMiss(rawMessage, config)) {
            return null;
        }
        return KB_MISS_REPLY;
    }

    private boolean hasWorkflowBindings(AgentAppConfigDto config) {
        if (config.getWorkflowBindings() == null || config.getWorkflowBindings().isEmpty()) {
            return false;
        }
        for (AgentAppConfigDto.WorkflowBindingDto binding : config.getWorkflowBindings()) {
            if (binding != null && binding.getWorkflowId() != null
                && StrUtil.isNotBlank(binding.getToolName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isKbSearchMiss(String rawMessage, AgentAppConfigDto config) {
        return !knowledgeSearchToolFactory.hasRelevantHits(config.getKbIds(), rawMessage);
    }

    private String buildUserMessage(String rawMessage, AgentAppConfigDto config) {
        if (config.getKbIds() == null || config.getKbIds().isEmpty()) {
            if (hasWorkflowBindings(config)) {
                return WF_ONLY_USER_PREFIX + rawMessage;
            }
            return rawMessage;
        }
        if (isKbSearchMiss(rawMessage, config)) {
            if (hasWorkflowBindings(config)) {
                return KB_MISS_USER_PREFIX + rawMessage;
            }
            return rawMessage;
        }
        String kbContext = knowledgeSearchToolFactory.searchFormattedContext(config.getKbIds(), rawMessage);
        return KB_CONTEXT_USER_PREFIX
            + "【知识库检索上下文】\n" + kbContext
            + "\n\n【用户问题】\n" + rawMessage;
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

    private String buildToolHint(AgentAppConfigDto config) {
        boolean hasKb = config.getKbIds() != null && !config.getKbIds().isEmpty();
        boolean hasWf = config.getWorkflowBindings() != null && !config.getWorkflowBindings().isEmpty();
        StringBuilder hint = new StringBuilder(TOOL_HINT_BASE);
        if (hasWf) {
            hint.append("\n\n你已绑定工作流工具，处理用户问题时 MUST 优先调用匹配的工作流工具获取结果：");
            for (AgentAppConfigDto.WorkflowBindingDto binding : config.getWorkflowBindings()) {
                if (binding == null || StrUtil.isBlank(binding.getToolName())) {
                    continue;
                }
                hint.append("\n- 工具 `").append(binding.getToolName()).append("`：")
                    .append(StrUtil.blankToDefault(binding.getDescription(), "执行关联工作流"));
            }
        }
        if (hasKb) {
            if (hasWf) {
                hint.append("\n\n可使用 `search_knowledge` 检索绑定的知识库。");
            }
            if (!hasWf) {
                hint.append("""
                    
                    【知识库约束】你只能依据系统注入的知识库检索结果回答。
                    禁止使用模型预训练知识、常识或外部信息。
                    若检索无相关内容，必须明确回复「未找到相关内容」。
                    """);
            }
        }
        if (hasWf && !hasKb) {
            hint.append("\n\n当前未绑定知识库，请勿声称知识库检索不可用；请直接调用上述工作流工具。");
        }
        return hint.toString();
    }

    private ToolCallback[] mergeTools(AgentAppConfigDto config, boolean preview) {
        boolean kbOnly = config.getKbIds() != null && !config.getKbIds().isEmpty() && !hasWorkflowBindings(config);
        // 仅绑知识库：检索上下文在发消息前已注入，不再注册 Tool，避免模型检索失败后改用常识作答
        ToolCallback[] kbTools = kbOnly ? new ToolCallback[0] : knowledgeSearchToolFactory.create(config.getKbIds());
        ToolCallback[] wfTools = workflowToolFactory.create(
            config.getWorkflowBindings(), config.getKbIds(), preview);
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

    private String resolveConfigJson(AiApp app, Boolean preview, String previewConfigJson) {
        if (Boolean.TRUE.equals(preview) && StrUtil.isNotBlank(previewConfigJson)) {
            return previewConfigJson;
        }
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

    /**
     * 解析智能体配置，兼容 kbIds 为字符串或数字（避免前端雪花 ID 精度丢失）。
     */
    private AgentAppConfigDto parseAgentConfig(String configJson) {
        if (StrUtil.isBlank(configJson)) {
            return new AgentAppConfigDto();
        }
        cn.hutool.json.JSONObject obj = JSONUtil.parseObj(configJson);
        AgentAppConfigDto config = obj.toBean(AgentAppConfigDto.class);
        if (obj.containsKey("chatModelId") && obj.get("chatModelId") != null) {
            String chatModelText = String.valueOf(obj.get("chatModelId")).trim();
            if (StrUtil.isNotBlank(chatModelText)) {
                try {
                    config.setChatModelId(Long.parseLong(chatModelText));
                } catch (NumberFormatException ignored) {
                    // 保留 toBean 解析结果
                }
            }
        }
        if (obj.containsKey("kbIds") && obj.get("kbIds") instanceof cn.hutool.json.JSONArray kbArr) {
            List<Long> kbIds = new ArrayList<>();
            for (Object item : kbArr) {
                if (item == null) {
                    continue;
                }
                String text = String.valueOf(item).trim();
                if (StrUtil.isBlank(text)) {
                    continue;
                }
                try {
                    kbIds.add(Long.parseLong(text));
                } catch (NumberFormatException ignored) {
                    // 跳过非法 ID
                }
            }
            config.setKbIds(kbIds);
        }
        if (obj.containsKey("workflowBindings") && obj.get("workflowBindings") instanceof cn.hutool.json.JSONArray wfArr) {
            List<AgentAppConfigDto.WorkflowBindingDto> bindings = new ArrayList<>();
            for (Object item : wfArr) {
                if (!(item instanceof cn.hutool.json.JSONObject wfObj)) {
                    continue;
                }
                AgentAppConfigDto.WorkflowBindingDto binding = wfObj.toBean(AgentAppConfigDto.WorkflowBindingDto.class);
                if (wfObj.containsKey("workflowId") && wfObj.get("workflowId") != null) {
                    String workflowIdText = String.valueOf(wfObj.get("workflowId")).trim();
                    if (StrUtil.isNotBlank(workflowIdText)) {
                        try {
                            binding.setWorkflowId(Long.parseLong(workflowIdText));
                        } catch (NumberFormatException ignored) {
                            binding.setWorkflowId(null);
                        }
                    }
                }
                if (binding.getWorkflowId() != null && StrUtil.isNotBlank(binding.getToolName())) {
                    bindings.add(binding);
                }
            }
            config.setWorkflowBindings(bindings);
        }
        return config;
    }

    private void sendEvent(SseEmitter emitter, String event, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(event).data(data, MediaType.APPLICATION_JSON));
    }
}
