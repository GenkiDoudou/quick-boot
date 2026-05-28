package io.github.genkidoudou.web.system.logininfor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.core.service.LoginLockService;
import io.github.genkidoudou.web.system.logininfor.dto.SysLogininforQueryBo;
import io.github.genkidoudou.web.system.logininfor.dto.SysLogininforVo;
import io.github.genkidoudou.web.system.logininfor.service.SysLogininforService;
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
 * 登录访问日志（监控）接口。
 */
@Tag(name = "登录日志")
@Validated
@RestController
@RequestMapping("/monitor/logininfor")
@RequiredArgsConstructor
public class SysLogininforController {

    private final SysLogininforService logininforService;
    private final LoginLockService loginLockService;

    @Operation(summary = "登录日志分页列表")
    @SaCheckPermission("monitor:logininfor:list")
    @GetMapping("/list")
    public R<PageInfo<SysLogininforVo>> list(@Validated SysLogininforQueryBo query) {
        return R.ok(logininforService.page(query));
    }

    @Operation(summary = "导出登录日志")
    @SaCheckPermission("monitor:logininfor:export")
    @PostMapping("/export")
    public void export(@Validated SysLogininforQueryBo query, HttpServletResponse response) {
        logininforService.export(query, response);
    }

    @Operation(summary = "删除登录日志（批量）")
    @SaCheckPermission("monitor:logininfor:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> infoIds) {
        logininforService.removeBatch(infoIds);
        return R.ok();
    }

    @Operation(summary = "清空登录日志")
    @SaCheckPermission("monitor:logininfor:remove")
    @PostMapping("/clean")
    public R<Void> clean() {
        logininforService.cleanAll();
        return R.ok();
    }

    @Operation(summary = "账户解锁（清除登录失败锁定缓存）")
    @SaCheckPermission("monitor:logininfor:unlock")
    @GetMapping("/unlock/{userName}")
    public R<Void> unlock(@Parameter(description = "登录用户名") @PathVariable String userName) {
        String name = loginLockService.normalizeUserName(userName);
        if (StrUtil.isBlank(name)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户名不能为空");
        }
        loginLockService.clearForUserName(name);
        return R.ok("已清除用户「" + name + "」的登录失败锁定状态");
    }
}
