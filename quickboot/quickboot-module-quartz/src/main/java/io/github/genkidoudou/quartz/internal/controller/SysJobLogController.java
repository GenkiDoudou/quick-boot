package io.github.genkidoudou.quartz.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.PageRequestMapping;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.web.DeprecatedApiSupport;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.quartz.internal.dto.SysJobLogQueryBo;
import io.github.genkidoudou.quartz.internal.dto.SysJobLogVo;
import io.github.genkidoudou.quartz.internal.service.SysJobLogService;
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
 * 调度日志（监控）接口。
 */
@Tag(name = "调度日志")
@Validated
@RestController
@RequestMapping("/monitor/jobLog")
@RequiredArgsConstructor
public class SysJobLogController {

    private final SysJobLogService jobLogService;

    @Operation(summary = "调度日志分页列表")
    @SaCheckPermission("monitor:job:query")
    @PostMapping("/page")
    public R<PageInfo<SysJobLogVo>> page(@RequestBody PageRequest<SysJobLogQueryBo> pageRequest) {
        return R.ok(jobLogService.page(toJobLogQuery(pageRequest)));
    }

    /**
     * @deprecated 请改用 POST {@code /monitor/jobLog/page}
     */
    @Deprecated
    @Operation(summary = "调度日志分页列表（兼容）", deprecated = true)
    @SaCheckPermission("monitor:job:query")
    @GetMapping("/list")
    public R<PageInfo<SysJobLogVo>> list(HttpServletResponse response, @Validated SysJobLogQueryBo query) {
        DeprecatedApiSupport.markDeprecated(response);
        return R.ok(jobLogService.page(query));
    }

    @Operation(summary = "调度日志详情")
    @SaCheckPermission("monitor:job:query")
    @GetMapping("/{jobLogId}")
    public R<SysJobLogVo> get(@Parameter(description = "日志主键") @PathVariable Long jobLogId) {
        return R.ok(jobLogService.getById(jobLogId));
    }

    @Operation(summary = "删除调度日志（批量）")
    @SaCheckPermission("monitor:job:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> jobLogIds) {
        jobLogService.removeBatch(jobLogIds);
        return R.ok();
    }

    @Operation(summary = "清空调度日志")
    @SaCheckPermission("monitor:job:remove")
    @PostMapping("/clean")
    public R<Void> clean() {
        jobLogService.cleanAll();
        return R.ok();
    }

    @Operation(summary = "导出调度日志")
    @SaCheckPermission("monitor:job:export")
    @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
    @PostMapping("/export")
    public void export(@Validated SysJobLogQueryBo query, HttpServletResponse response) {
        jobLogService.export(query, response);
    }

    private static SysJobLogQueryBo toJobLogQuery(PageRequest<SysJobLogQueryBo> pageRequest) {
        SysJobLogQueryBo param = pageRequest != null && pageRequest.getParam() != null
            ? pageRequest.getParam()
            : new SysJobLogQueryBo();
        param.setPageNum(PageRequestMapping.pageNum(pageRequest));
        param.setPageSize(PageRequestMapping.pageSize(pageRequest));
        return param;
    }
}
