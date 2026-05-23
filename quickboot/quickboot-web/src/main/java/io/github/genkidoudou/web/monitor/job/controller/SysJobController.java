package io.github.genkidoudou.web.monitor.job.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.monitor.job.dto.SysJobInvokeTargetVo;
import io.github.genkidoudou.web.monitor.job.dto.SysJobQueryBo;
import io.github.genkidoudou.web.monitor.job.dto.SysJobRunBo;
import io.github.genkidoudou.web.monitor.job.dto.SysJobSaveBo;
import io.github.genkidoudou.web.monitor.job.dto.SysJobStatusBo;
import io.github.genkidoudou.web.monitor.job.dto.SysJobVo;
import io.github.genkidoudou.web.monitor.job.service.SysJobService;
import io.github.genkidoudou.web.monitor.job.support.JobInvokeTargetRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;

/**
 * 定时任务（监控）接口。
 */
@Tag(name = "定时任务")
@Validated
@RestController
@RequestMapping("/monitor/job")
@RequiredArgsConstructor
public class SysJobController {

    private final SysJobService jobService;
    private final JobInvokeTargetRegistry invokeTargetRegistry;

    @Operation(summary = "定时任务分页列表")
    @SaCheckPermission("monitor:job:list")
    @GetMapping("/list")
    public R<PageInfo<SysJobVo>> list(@Validated SysJobQueryBo query) {
        return R.ok(jobService.page(query));
    }

    @Operation(summary = "可选调用目标（ITask Bean）")
    @SaCheckPermission("monitor:job:list")
    @GetMapping("/invokeTargets")
    public R<List<SysJobInvokeTargetVo>> invokeTargets() {
        return R.ok(invokeTargetRegistry.listTargets());
    }

    @Operation(summary = "定时任务详情")
    @SaCheckPermission("monitor:job:query")
    @GetMapping("/{jobId}")
    public R<SysJobVo> get(@Parameter(description = "任务主键") @PathVariable Long jobId) {
        return R.ok(jobService.getById(jobId));
    }

    @Operation(summary = "新增定时任务")
    @SaCheckPermission("monitor:job:add")
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysJobSaveBo bo) {
        jobService.add(bo);
        return R.ok();
    }

    @Operation(summary = "修改定时任务")
    @SaCheckPermission("monitor:job:edit")
    @PostMapping("/edit")
    public R<Void> edit(@Validated @RequestBody SysJobSaveBo bo) {
        jobService.edit(bo);
        return R.ok();
    }

    @Operation(summary = "删除定时任务（批量）")
    @SaCheckPermission("monitor:job:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> jobIds) {
        jobService.removeBatch(jobIds);
        return R.ok();
    }

    @Operation(summary = "修改任务状态")
    @SaCheckPermission("monitor:job:changeStatus")
    @PostMapping("/changeStatus")
    public R<Void> changeStatus(@Validated @RequestBody SysJobStatusBo bo) {
        jobService.changeStatus(bo.getJobId(), bo.getStatus());
        return R.ok();
    }

    @Operation(summary = "立即执行一次")
    @SaCheckPermission("monitor:job:changeStatus")
    @PostMapping("/run")
    public R<String> run(@Validated @RequestBody SysJobRunBo bo) {
        jobService.runOnce(bo.getJobId());
        return R.ok("执行成功");
    }

    @Operation(summary = "导出定时任务")
    @SaCheckPermission("monitor:job:export")
    @PostMapping("/export")
    public void export(@Validated SysJobQueryBo query, HttpServletResponse response) {
        jobService.export(query, response);
    }
}
