package io.github.genkidoudou.web.system.operlog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.web.system.operlog.dto.SysOperLogQueryBo;
import io.github.genkidoudou.web.system.operlog.dto.SysOperLogVo;
import io.github.genkidoudou.web.system.operlog.service.SysOperLogService;
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
 * 操作日志（监控）接口。
 */
@Tag(name = "操作日志")
@Validated
@RestController
@RequestMapping("/monitor/operlog")
@RequiredArgsConstructor
@IgnoreLogger(type = IgnoreLogger.Type.ALL)
public class SysOperLogController {

    private final SysOperLogService operLogService;

    @Operation(summary = "操作日志分页列表")
    @SaCheckPermission("monitor:operlog:query")
    @GetMapping("/list")
    public R<PageInfo<SysOperLogVo>> list(@Validated SysOperLogQueryBo query) {
        return R.ok(operLogService.page(query));
    }

    @Operation(summary = "操作日志详情")
    @SaCheckPermission("monitor:operlog:query")
    @GetMapping("/{operId}")
    public R<SysOperLogVo> get(
        @Parameter(description = "日志主键") @PathVariable("operId") Long operId
    ) {
        return R.ok(operLogService.getById(operId));
    }

    @Operation(summary = "导出操作日志")
    @SaCheckPermission("monitor:operlog:export")
    @PostMapping("/export")
    public void export(@Validated SysOperLogQueryBo query, HttpServletResponse response) {
        operLogService.export(query, response);
    }

    @Operation(summary = "删除操作日志（批量）")
    @SaCheckPermission("monitor:operlog:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> operIds) {
        operLogService.removeBatch(operIds);
        return R.ok();
    }

    @Operation(summary = "清空操作日志")
    @SaCheckPermission("monitor:operlog:remove")
    @PostMapping("/clean")
    public R<Void> clean() {
        operLogService.cleanAll();
        return R.ok();
    }
}
