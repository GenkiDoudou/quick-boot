package io.github.genkidoudou.web.aiapp.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.aiapp.dto.AiAppSessionBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppSessionVo;
import io.github.genkidoudou.web.aiapp.service.AiAppSessionService;
import io.github.genkidoudou.web.aiapp.support.AiAppUserSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 应用会话管理接口。
 */
@Tag(name = "AI 应用会话")
@Validated
@RestController
@RequestMapping("/ai/app/session")
@RequiredArgsConstructor
public class AiAppSessionController {

    private final AiAppSessionService sessionService;
    private final AiAppUserSupport userSupport;

    @Operation(summary = "会话列表")
    @SaCheckPermission("aiapp:chat")
    @GetMapping("/list")
    public R<List<AiAppSessionVo>> list(
        @Parameter(description = "应用ID") @RequestParam @Min(1) Long appId) {
        return R.ok(sessionService.listByAppAndUser(appId, userSupport.currentUserKey()));
    }

    @Operation(summary = "新建会话")
    @SaCheckPermission("aiapp:chat")
    @PostMapping("/add")
    public R<Long> add(@Validated @RequestBody AiAppSessionBo req) {
        return R.ok(sessionService.add(req, userSupport.currentUserKey()));
    }

    @Operation(summary = "删除会话")
    @SaCheckPermission("aiapp:chat")
    @PostMapping("/remove")
    public R<Void> remove(@RequestParam @Min(1) Long sessionId) {
        sessionService.remove(sessionId, userSupport.currentUserKey());
        return R.ok();
    }
}
