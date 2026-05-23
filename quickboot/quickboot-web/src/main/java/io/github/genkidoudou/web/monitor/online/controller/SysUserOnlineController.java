package io.github.genkidoudou.web.monitor.online.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.web.monitor.online.dto.ForceLogoutBo;
import io.github.genkidoudou.web.monitor.online.dto.SysUserOnlineQueryBo;
import io.github.genkidoudou.web.monitor.online.dto.SysUserOnlineVo;
import io.github.genkidoudou.web.monitor.online.service.SysUserOnlineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线用户监控接口。
 */
@Tag(name = "在线用户")
@Validated
@RestController
@RequestMapping("/monitor/online")
@RequiredArgsConstructor
@IgnoreLogger(type = IgnoreLogger.Type.ALL)
public class SysUserOnlineController {

    private final SysUserOnlineService onlineService;

    @Operation(summary = "在线用户分页列表")
    @SaCheckPermission("monitor:online:list")
    @GetMapping("/list")
    public R<PageInfo<SysUserOnlineVo>> list(@Validated SysUserOnlineQueryBo query) {
        return R.ok(onlineService.page(query));
    }

    @Operation(summary = "强退在线会话")
    @SaCheckPermission("monitor:online:forceLogout")
    @PostMapping("/forceLogout")
    public R<Void> forceLogout(@Validated @RequestBody ForceLogoutBo req) {
        onlineService.forceLogout(req);
        return R.ok();
    }
}
