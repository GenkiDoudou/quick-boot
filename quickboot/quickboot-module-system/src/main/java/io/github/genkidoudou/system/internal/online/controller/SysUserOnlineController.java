package io.github.genkidoudou.system.internal.online.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.PageRequestMapping;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.idempotency.Idempotent;
import io.github.genkidoudou.common.web.DeprecatedApiSupport;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.system.internal.online.dto.ForceLogoutBo;
import io.github.genkidoudou.system.internal.online.dto.SysUserOnlineQueryBo;
import io.github.genkidoudou.system.internal.online.dto.SysUserOnlineVo;
import io.github.genkidoudou.system.internal.online.service.SysUserOnlineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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

    /**
     * 在线用户分页列表。
     *
     * @param query 用户名、IP 等筛选与分页参数
     * @return 当前活跃会话分页
     */
    @Operation(summary = "在线用户分页列表")
    @SaCheckPermission("monitor:online:list")
    @PostMapping("/page")
    public R<PageInfo<SysUserOnlineVo>> page(@RequestBody PageRequest<SysUserOnlineQueryBo> pageRequest) {
        return R.ok(onlineService.page(toOnlineQuery(pageRequest)));
    }

    /**
     * @deprecated 请改用 POST {@code /monitor/online/page}
     */
    @Deprecated
    @Operation(summary = "在线用户分页列表（兼容）", deprecated = true)
    @SaCheckPermission("monitor:online:list")
    @GetMapping("/list")
    public R<PageInfo<SysUserOnlineVo>> list(HttpServletResponse response, @Validated SysUserOnlineQueryBo query) {
        DeprecatedApiSupport.markDeprecated(response);
        return R.ok(onlineService.page(query));
    }

    /**
     * 强退指定在线会话。
     *
     * @param req 待强退的 token 或会话标识
     * @return 空；副作用为注销对应 Sa-Token 会话
     */
    @Operation(summary = "强退在线会话")
    @SaCheckPermission("monitor:online:forceLogout")
    @Idempotent(ttlSeconds = 10, key = "#userId + ':force:' + #req.tokenId", message = "请勿重复提交")
    @PostMapping("/forceLogout")
    public R<Void> forceLogout(@Validated @RequestBody ForceLogoutBo req) {
        onlineService.forceLogout(req);
        return R.ok();
    }

    private static SysUserOnlineQueryBo toOnlineQuery(PageRequest<SysUserOnlineQueryBo> pageRequest) {
        SysUserOnlineQueryBo param = pageRequest != null && pageRequest.getParam() != null
            ? pageRequest.getParam()
            : new SysUserOnlineQueryBo();
        param.setPageNum(PageRequestMapping.pageNum(pageRequest));
        param.setPageSize(PageRequestMapping.pageSize(pageRequest));
        return param;
    }
}
