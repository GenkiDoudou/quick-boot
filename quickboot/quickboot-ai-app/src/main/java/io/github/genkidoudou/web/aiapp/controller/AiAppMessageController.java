package io.github.genkidoudou.web.aiapp.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.aiapp.dto.AiAppMessageVo;
import io.github.genkidoudou.web.aiapp.service.AiAppMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 应用消息查询接口。
 */
@Tag(name = "AI 应用消息")
@Validated
@RestController
@RequestMapping("/ai/app/message")
@RequiredArgsConstructor
public class AiAppMessageController {

    private final AiAppMessageService messageService;

    @Operation(summary = "会话消息列表")
    @SaCheckPermission("aiapp:chat")
    @GetMapping("/list")
    public R<List<AiAppMessageVo>> list(
        @Parameter(description = "会话ID") @RequestParam @Min(1) Long sessionId) {
        return R.ok(messageService.listBySession(sessionId));
    }
}
