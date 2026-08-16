package io.github.genkidoudou.monitor.internal.slowsql.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.monitor.internal.slowsql.dto.SysSlowSqlQueryBo;
import io.github.genkidoudou.monitor.internal.slowsql.dto.SysSlowSqlVo;
import io.github.genkidoudou.monitor.internal.slowsql.service.SysSlowSqlService;
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

    /**
     * 分页查询慢 SQL 记录。
     *
     * @param query 筛选与分页条件
     * @return 慢 SQL 分页列表
     */
    @Operation(summary = "慢 SQL 分页列表")
    @SaCheckPermission("monitor:slowSql:query")
    @GetMapping("/list")
    public R<PageInfo<SysSlowSqlVo>> list(@Validated SysSlowSqlQueryBo query) {
        return R.ok(slowSqlService.page(query));
    }

    /**
     * 按主键查询慢 SQL 详情。
     *
     * @param slowId 慢 SQL 主键
     * @return 慢 SQL 详情；不存在时抛业务异常
     */
    @Operation(summary = "慢 SQL 详情")
    @SaCheckPermission("monitor:slowSql:query")
    @GetMapping("/{slowId}")
    public R<SysSlowSqlVo> get(@Parameter(description = "主键") @PathVariable("slowId") Long slowId) {
        return R.ok(slowSqlService.getById(slowId));
    }

    /**
     * 按条件导出慢 SQL 为 Excel。
     *
     * @param query    筛选条件
     * @param response HTTP 响应，直接写入文件流
     */
    @Operation(summary = "导出慢 SQL")
    @SaCheckPermission("monitor:slowSql:export")
    @PostMapping("/export")
    public void export(@Validated SysSlowSqlQueryBo query, HttpServletResponse response) {
        slowSqlService.export(query, response);
    }

    /**
     * 批量删除慢 SQL 记录。
     *
     * @param slowIds 待删除主键列表
     * @return 空成功响应；副作用：物理删除 sys_slow_sql 行
     */
    @Operation(summary = "批量删除慢 SQL")
    @SaCheckPermission("monitor:slowSql:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> slowIds) {
        slowSqlService.removeBatch(slowIds);
        return R.ok();
    }

    /**
     * 清空全部慢 SQL 记录。
     *
     * @return 空成功响应；副作用：物理删除 sys_slow_sql 全表
     */
    @Operation(summary = "清空慢 SQL")
    @SaCheckPermission("monitor:slowSql:remove")
    @PostMapping("/clean")
    public R<Void> clean() {
        slowSqlService.cleanAll();
        return R.ok();
    }
}
