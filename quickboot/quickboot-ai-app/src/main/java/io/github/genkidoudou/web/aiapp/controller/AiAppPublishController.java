package io.github.genkidoudou.web.aiapp.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.aiapp.dto.AiAppPublishVo;
import io.github.genkidoudou.web.aiapp.service.AiAppPublishService;
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

/**
 * AI 应用发布与嵌入配置接口。
 */
@Tag(name = "AI 应用发布")
@Validated
@RestController
@RequestMapping("/ai/app/publish")
@RequiredArgsConstructor
public class AiAppPublishController {

    private final AiAppPublishService publishService;

    @Operation(summary = "获取嵌入配置")
    @SaCheckPermission("aiapp:publish")
    @GetMapping("/getEmbedInfo")
    public R<AiAppPublishVo> getEmbedInfo(
        @Parameter(description = "应用ID") @RequestParam @Min(1) Long appId) {
        return R.ok(publishService.getEmbedInfo(appId));
    }

    @Operation(summary = "保存嵌入/菜单配置")
    @SaCheckPermission("aiapp:publish")
    @PostMapping("/saveEmbed")
    public R<Void> saveEmbed(@Validated @RequestBody AiAppPublishVo req) {
        publishService.saveEmbed(req);
        return R.ok();
    }
}
