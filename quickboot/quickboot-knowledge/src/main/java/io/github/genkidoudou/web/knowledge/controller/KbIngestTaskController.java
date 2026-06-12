package io.github.genkidoudou.web.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.knowledge.dto.KbIngestTaskVo;
import io.github.genkidoudou.web.knowledge.service.KbIngestTaskService;
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

/**
 * 异步入库任务查询接口。
 */
@Tag(name = "知识库入库任务")
@Validated
@RestController
@RequestMapping("/knowledge/task")
@RequiredArgsConstructor
public class KbIngestTaskController {

    private final KbIngestTaskService service;

    @Operation(summary = "查询入库任务进度")
    @SaCheckPermission("knowledge:doc:list")
    @GetMapping("/getInfo")
    public R<KbIngestTaskVo> getInfo(
        @Parameter(description = "任务ID") @RequestParam @Min(1) Long taskId) {
        return R.ok(service.getInfo(taskId));
    }
}
