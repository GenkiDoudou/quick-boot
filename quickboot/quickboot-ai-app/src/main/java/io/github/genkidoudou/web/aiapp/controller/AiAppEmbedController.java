package io.github.genkidoudou.web.aiapp.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.aiapp.constants.AiAppType;
import io.github.genkidoudou.web.aiapp.domain.AiApp;
import io.github.genkidoudou.web.aiapp.domain.AiAppPublish;
import io.github.genkidoudou.web.aiapp.dto.AgentAppConfigDto;
import io.github.genkidoudou.web.aiapp.dto.AiAppChatBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppEmbedPublicVo;
import io.github.genkidoudou.web.aiapp.dto.AiAppMessageVo;
import io.github.genkidoudou.web.aiapp.dto.AiAppSessionBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppSessionVo;
import io.github.genkidoudou.web.aiapp.dto.WorkflowAppConfigDto;
import io.github.genkidoudou.web.aiapp.service.AiAppChatService;
import io.github.genkidoudou.web.aiapp.service.AiAppMessageService;
import io.github.genkidoudou.web.aiapp.service.AiAppPublishService;
import io.github.genkidoudou.web.aiapp.service.AiAppService;
import io.github.genkidoudou.web.aiapp.service.AiAppSessionService;
import io.github.genkidoudou.web.aiapp.service.AiAppWorkflowChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 应用嵌入公开 API（无需登录，校验 token 与 Origin）。
 */
@Tag(name = "AI 应用嵌入")
@Validated
@RestController
@RequestMapping("/ai/embed")
@RequiredArgsConstructor
public class AiAppEmbedController {

    private final AiAppPublishService publishService;
    private final AiAppService appService;
    private final AiAppChatService chatService;
    private final AiAppWorkflowChatService workflowChatService;
    private final AiAppSessionService sessionService;
    private final AiAppMessageService messageService;

    @Operation(summary = "嵌入：应用公开信息")
    @GetMapping("/{token}/app")
    public R<AiAppEmbedPublicVo> getEmbedApp(@PathVariable String token) {
        AiAppPublish publish = publishService.requireByToken(token);
        AiApp app = appService.requireApp(publish.getAppId());
        AiAppEmbedPublicVo vo = new AiAppEmbedPublicVo();
        vo.setAppId(app.getId());
        vo.setName(app.getName());
        vo.setAppType(app.getAppType());
        String configJson = StrUtil.blankToDefault(app.getPublishedConfigJson(), app.getConfigJson());
        if (StrUtil.isNotBlank(configJson)) {
            if (AiAppType.WORKFLOW.equals(app.getAppType())) {
                WorkflowAppConfigDto cfg = JSONUtil.toBean(configJson, WorkflowAppConfigDto.class);
                vo.setOpeningMessage(cfg.getOpeningMessage());
                vo.setSuggestedQuestions(cfg.getSuggestedQuestions());
            } else {
                AgentAppConfigDto cfg = JSONUtil.toBean(configJson, AgentAppConfigDto.class);
                vo.setOpeningMessage(cfg.getOpeningMessage());
                vo.setSuggestedQuestions(cfg.getSuggestedQuestions());
                vo.setQuickCommands(cfg.getQuickCommands());
                vo.setChatModelId(cfg.getChatModelId());
            }
        }
        return R.ok(vo);
    }

    @Operation(summary = "嵌入：新建会话")
    @PostMapping("/{token}/session/add")
    public R<Long> addSession(@PathVariable String token,
                                @RequestHeader("X-Embed-Visitor-Id") @NotBlank String visitorId) {
        AiAppPublish publish = publishService.requireByToken(token);
        AiAppSessionBo bo = new AiAppSessionBo();
        bo.setAppId(publish.getAppId());
        bo.setTitle("新会话");
        return R.ok(sessionService.add(bo, embedUserKey(visitorId)));
    }

    @Operation(summary = "嵌入：会话列表")
    @GetMapping("/{token}/session/list")
    public R<List<AiAppSessionVo>> listSessions(@PathVariable String token,
                                                @RequestHeader("X-Embed-Visitor-Id") @NotBlank String visitorId) {
        AiAppPublish publish = publishService.requireByToken(token);
        return R.ok(sessionService.listByAppAndUser(publish.getAppId(), embedUserKey(visitorId)));
    }

    @Operation(summary = "嵌入：消息列表")
    @GetMapping("/{token}/message/list")
    public R<List<AiAppMessageVo>> listMessages(@PathVariable String token,
                                                @RequestParam Long sessionId,
                                                @RequestHeader("X-Embed-Visitor-Id") @NotBlank String visitorId) {
        publishService.requireByToken(token);
        sessionService.requireSession(sessionId, embedUserKey(visitorId));
        return R.ok(messageService.listBySession(sessionId));
    }

    @Operation(summary = "嵌入 SSE 对话")
    @PostMapping(value = "/{token}/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter embedChatStream(@PathVariable String token,
                                      @Valid @RequestBody AiAppChatBo req,
                                      HttpServletRequest request) {
        AiAppPublish publish = publishService.requireByToken(token);
        publishService.validateOrigin(publish, request.getHeader("Origin"));
        req.setAppId(publish.getAppId());
        req.setPreview(false);
        String userKey = embedUserKey(request.getHeader("X-Embed-Visitor-Id"));
        sessionService.requireSession(req.getSessionId(), userKey);
        AiApp app = appService.requireApp(publish.getAppId());
        if (AiAppType.WORKFLOW.equals(app.getAppType())) {
            return workflowChatService.streamChat(req, userKey);
        }
        return chatService.streamChat(req, userKey);
    }

    private String embedUserKey(String visitorId) {
        if (StrUtil.isBlank(visitorId)) {
            throw new IllegalArgumentException("缺少访客标识 X-Embed-Visitor-Id");
        }
        return "embed:" + visitorId.trim();
    }
}
