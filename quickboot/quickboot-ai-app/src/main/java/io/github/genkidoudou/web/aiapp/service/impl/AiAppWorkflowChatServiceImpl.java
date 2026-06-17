package io.github.genkidoudou.web.aiapp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.aiapp.config.AiAppProperties;
import io.github.genkidoudou.web.aiapp.constants.AiAppStatus;
import io.github.genkidoudou.web.aiapp.constants.AiAppType;
import io.github.genkidoudou.web.aiapp.domain.AiApp;
import io.github.genkidoudou.web.aiapp.domain.AiAppSession;
import io.github.genkidoudou.web.aiapp.dto.AiAppChatBo;
import io.github.genkidoudou.web.aiapp.dto.WorkflowAppConfigDto;
import io.github.genkidoudou.web.aiapp.service.AiAppMessageService;
import io.github.genkidoudou.web.aiapp.service.AiAppService;
import io.github.genkidoudou.web.aiapp.service.AiAppSessionService;
import io.github.genkidoudou.web.aiapp.service.AiAppWorkflowChatService;
import io.github.genkidoudou.web.workflow.dto.WfRunDebugBo;
import io.github.genkidoudou.web.workflow.dto.WfRunDetailVo;
import io.github.genkidoudou.web.workflow.service.WorkflowRunService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI 高级编排应用聊天服务实现：消息触发 WorkflowEngine 已发布版本。
 */
@Service
public class AiAppWorkflowChatServiceImpl implements AiAppWorkflowChatService {

    private final AiAppProperties properties;
    private final AiAppService appService;
    private final AiAppSessionService sessionService;
    private final AiAppMessageService messageService;
    private final WorkflowRunService workflowRunService;

    public AiAppWorkflowChatServiceImpl(AiAppProperties properties,
                                        AiAppService appService,
                                        AiAppSessionService sessionService,
                                        AiAppMessageService messageService,
                                        WorkflowRunService workflowRunService) {
        this.properties = properties;
        this.appService = appService;
        this.sessionService = sessionService;
        this.messageService = messageService;
        this.workflowRunService = workflowRunService;
    }

    @Override
    public SseEmitter streamChat(AiAppChatBo req, String userKey) {
        SseEmitter emitter = new SseEmitter(properties.getChatTimeoutMs());
        CompletableFuture.runAsync(() -> runWorkflowChat(req, userKey, emitter));
        return emitter;
    }

    private void runWorkflowChat(AiAppChatBo req, String userKey, SseEmitter emitter) {
        try {
            AiApp app = appService.requireApp(req.getAppId());
            if (!AiAppType.WORKFLOW.equals(app.getAppType())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "非高级编排应用不可使用 workflow 聊天");
            }
            validateAppAccessible(app, req.getPreview());
            AiAppSession session = sessionService.requireSession(req.getSessionId(), userKey);
            if (!session.getAppId().equals(app.getId())) {
                throw new WarningException(ErrorCodes.Biz.AI_APP_NOT_FOUND, "会话与应用不匹配");
            }

            String configJson = resolveConfigJson(app, req.getPreview());
            WorkflowAppConfigDto config = JSONUtil.toBean(configJson, WorkflowAppConfigDto.class);
            if (config.getWorkflowId() == null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "未配置 workflowId");
            }

            messageService.saveUserMessage(session.getId(), req.getMessage());
            sessionService.updateTitleIfBlank(session.getId(), req.getMessage());

            WfRunDebugBo runBo = new WfRunDebugBo();
            runBo.setWorkflowId(config.getWorkflowId());
            runBo.setUseDraft(false);
            runBo.setStream(false);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("query", req.getMessage());
            runBo.setInputs(inputs);

            WfRunDetailVo runResult = workflowRunService.debugRun(runBo);
            String answer = extractAnswer(runResult);
            if (StrUtil.isBlank(answer)) {
                answer = "工作流执行完成，但未产生文本输出";
            }

            emitDeltaChunks(emitter, answer);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("runId", runResult.getRunId());
            Long assistantMsgId = messageService.saveAssistantMessage(
                session.getId(), answer, JSONUtil.toJsonStr(metadata));

            sendEvent(emitter, "done", Map.of("messageId", assistantMsgId, "runId", runResult.getRunId()));
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

    private String extractAnswer(WfRunDetailVo runResult) {
        if (runResult.getOutputs() == null || runResult.getOutputs().isEmpty()) {
            return "";
        }
        Object answer = runResult.getOutputs().get("answer");
        if (answer != null) {
            return String.valueOf(answer);
        }
        Object text = runResult.getOutputs().get("text");
        if (text != null) {
            return String.valueOf(text);
        }
        Object output = runResult.getOutputs().get("output");
        return output == null ? JSONUtil.toJsonStr(runResult.getOutputs()) : String.valueOf(output);
    }

    private void emitDeltaChunks(SseEmitter emitter, String answer) throws IOException {
        int chunkSize = 20;
        for (int i = 0; i < answer.length(); i += chunkSize) {
            String chunk = answer.substring(i, Math.min(i + chunkSize, answer.length()));
            sendEvent(emitter, "delta", Map.of("content", chunk));
        }
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
        throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "应用未发布或配置为空");
    }

    private void sendEvent(SseEmitter emitter, String event, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(event).data(data, MediaType.APPLICATION_JSON));
    }
}
