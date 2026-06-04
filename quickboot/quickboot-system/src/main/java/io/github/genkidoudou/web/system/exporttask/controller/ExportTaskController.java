package io.github.genkidoudou.web.system.exporttask.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.system.exporttask.dto.ExportSubmitRequestBo;
import io.github.genkidoudou.web.system.exporttask.dto.ExportTaskQueryBo;
import io.github.genkidoudou.web.system.exporttask.dto.ExportTaskVo;
import io.github.genkidoudou.web.system.exporttask.service.ExportOrchestratorService;
import io.github.genkidoudou.web.system.exporttask.support.ExportSubmitOutcome;
import io.github.genkidoudou.web.system.exporttask.support.ExportSubmitResponseWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 平台 Excel 导出任务 API。
 */
@Tag(name = "导出任务")
@Validated
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportTaskController {

    private final ExportOrchestratorService orchestratorService;

    @Operation(summary = "提交 Excel 导出（同步返回 xlsx 流，异步返回 JSON taskId）")
    @SaCheckPermission("system:ioCenter:submit")
    @PostMapping("/submit")
    public void submit(@Validated @RequestBody ExportSubmitRequestBo req, HttpServletResponse response)
        throws IOException {
        ExportSubmitOutcome outcome = orchestratorService.submitForResponse(
            req.getBizType(), req.getQueryParams(), req.getMode(), req.getSyncMaxRows());
        ExportSubmitResponseWriter.write(response, outcome);
    }

    @Operation(summary = "查询导出任务")
    @SaCheckPermission("system:ioCenter:list")
    @GetMapping("/task/{taskId}")
    public R<ExportTaskVo> getTask(@PathVariable Long taskId) {
        return R.ok(orchestratorService.getTask(taskId));
    }

    @Operation(summary = "导出任务分页列表")
    @SaCheckPermission("system:ioCenter:list")
    @GetMapping("/task/list")
    public R<PageInfo<ExportTaskVo>> list(@Validated ExportTaskQueryBo query) {
        return R.ok(orchestratorService.listTasks(query));
    }
}
