package io.github.genkidoudou.web.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.workflow.dto.WfRunAsyncBo;
import io.github.genkidoudou.web.workflow.dto.WfRunDebugBo;
import io.github.genkidoudou.web.workflow.dto.WfRunDetailVo;
import io.github.genkidoudou.web.workflow.dto.WfRunQueryBo;
import io.github.genkidoudou.web.workflow.dto.WfRunVo;
import io.github.genkidoudou.web.workflow.service.WorkflowRunService;
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
 * 工作流运行接口：Debug、异步、查询。
 */
@Tag(name = "工作流运行")
@Validated
@RestController
@RequestMapping("/workflow/run")
@RequiredArgsConstructor
public class WfRunController {

    private final WorkflowRunService runService;

    @Operation(summary = "同步 Debug 运行")
    @SaCheckPermission("workflow:run")
    @PostMapping("/debug")
    public R<WfRunDetailVo> debug(@Validated @RequestBody WfRunDebugBo req) {
        return R.ok(runService.debugRun(req));
    }

    @Operation(summary = "异步运行")
    @SaCheckPermission("workflow:run")
    @PostMapping("/async")
    public R<WfRunVo> async(@Validated @RequestBody WfRunAsyncBo req) {
        return R.ok(runService.asyncRun(req));
    }

    @Operation(summary = "运行详情")
    @SaCheckPermission("workflow:query")
    @GetMapping("/getInfo")
    public R<WfRunDetailVo> getInfo(
        @Parameter(description = "运行ID") @RequestParam @Min(1) Long runId) {
        return R.ok(runService.getInfo(runId));
    }

    @Operation(summary = "运行历史分页")
    @SaCheckPermission("workflow:list")
    @GetMapping("/list")
    public R<PageInfo<WfRunVo>> list(@Validated WfRunQueryBo query) {
        return R.ok(runService.page(query));
    }
}
