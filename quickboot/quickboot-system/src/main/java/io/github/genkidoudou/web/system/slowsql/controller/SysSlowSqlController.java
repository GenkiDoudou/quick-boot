package io.github.genkidoudou.web.system.slowsql.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.web.system.slowsql.dto.SysSlowSqlQueryBo;
import io.github.genkidoudou.web.system.slowsql.dto.SysSlowSqlVo;
import io.github.genkidoudou.web.system.slowsql.service.SysSlowSqlService;
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
 * 慢 SQL 监控接口。
 */
@Tag(name = "慢SQL日志")
@Validated
@RestController
@RequestMapping("/monitor/slowSql")
@RequiredArgsConstructor
@IgnoreLogger(type = IgnoreLogger.Type.ALL)
public class SysSlowSqlController {

    private final SysSlowSqlService slowSqlService;

    @Operation(summary = "慢 SQL 分页列表")
    @SaCheckPermission("monitor:slowSql:query")
    @GetMapping("/list")
    public R<PageInfo<SysSlowSqlVo>> list(@Validated SysSlowSqlQueryBo query) {
        return R.ok(slowSqlService.page(query));
    }

    @Operation(summary = "慢 SQL 详情")
    @SaCheckPermission("monitor:slowSql:query")
    @GetMapping("/{slowId}")
    public R<SysSlowSqlVo> get(@Parameter(description = "主键") @PathVariable("slowId") Long slowId) {
        return R.ok(slowSqlService.getById(slowId));
    }

    @Operation(summary = "导出慢 SQL")
    @SaCheckPermission("monitor:slowSql:export")
    @PostMapping("/export")
    public void export(@Validated SysSlowSqlQueryBo query, HttpServletResponse response) {
        slowSqlService.export(query, response);
    }

    @Operation(summary = "批量删除慢 SQL")
    @SaCheckPermission("monitor:slowSql:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> slowIds) {
        slowSqlService.removeBatch(slowIds);
        return R.ok();
    }

    @Operation(summary = "清空慢 SQL")
    @SaCheckPermission("monitor:slowSql:remove")
    @PostMapping("/clean")
    public R<Void> clean() {
        slowSqlService.cleanAll();
        return R.ok();
    }
}
