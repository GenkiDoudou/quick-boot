package io.github.genkidoudou.web.aiapp.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.aiapp.constants.AiAppType;
import io.github.genkidoudou.web.aiapp.domain.AiApp;
import io.github.genkidoudou.web.aiapp.dto.AiAppBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppChatBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppDetailVo;
import io.github.genkidoudou.web.aiapp.dto.AiAppPublishBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppQueryBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppVo;
import io.github.genkidoudou.web.aiapp.service.AiAppChatService;
import io.github.genkidoudou.web.aiapp.service.AiAppService;
import io.github.genkidoudou.web.aiapp.service.AiAppWorkflowChatService;
import io.github.genkidoudou.web.aiapp.support.AiAppUserSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 应用管理接口。
 */
@Tag(name = "AI 应用管理")
@Validated
@RestController
@RequestMapping("/ai/app")
@RequiredArgsConstructor
public class AiAppController {

    private final AiAppService appService;
    private final AiAppChatService chatService;
    private final AiAppWorkflowChatService workflowChatService;
    private final AiAppUserSupport userSupport;

    @Operation(summary = "AI 应用分页列表")
    @SaCheckPermission("aiapp:list")
    @GetMapping("/list")
    public R<PageInfo<AiAppVo>> list(@Validated AiAppQueryBo query) {
        return R.ok(appService.page(query));
    }

    @Operation(summary = "AI 应用详情")
    @SaCheckPermission("aiapp:query")
    @GetMapping("/getInfo")
    public R<AiAppDetailVo> getInfo(
        @Parameter(description = "应用ID") @RequestParam @Min(1) Long appId) {
        return R.ok(appService.getDetail(appId));
    }

    @Operation(summary = "新增 AI 应用")
    @SaCheckPermission("aiapp:add")
    @PostMapping("/add")
    public R<Long> add(@Validated(AddGroup.class) @RequestBody AiAppBo req) {
        return R.ok(appService.add(req));
    }

    @Operation(summary = "修改 AI 应用")
    @SaCheckPermission("aiapp:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody AiAppBo req) {
        appService.update(req);
        return R.ok();
    }

    @Operation(summary = "发布 AI 应用")
    @SaCheckPermission("aiapp:publish")
    @PostMapping("/publish")
    public R<Void> publish(@Validated @RequestBody AiAppPublishBo req) {
        appService.publish(req);
        return R.ok();
    }

    @Operation(summary = "删除 AI 应用")
    @SaCheckPermission("aiapp:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> appIds) {
        appService.removeBatch(appIds);
        return R.ok();
    }

    @Operation(summary = "SSE 对话（智能体/高级编排）")
    @SaCheckPermission("aiapp:chat")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Validated @RequestBody AiAppChatBo req) {
        AiApp app = appService.requireApp(req.getAppId());
        String userKey = userSupport.currentUserKey();
        if (AiAppType.WORKFLOW.equals(app.getAppType())) {
            return workflowChatService.streamChat(req, userKey);
        }
        return chatService.streamChat(req, userKey);
    }
}
