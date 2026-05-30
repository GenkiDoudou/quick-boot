package io.github.genkidoudou.web.monitor.clienttrack.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackReportBo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.SysClientTrackQueryBo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.SysClientTrackVo;
import io.github.genkidoudou.web.monitor.clienttrack.service.SysClientTrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前端用户行为监控接口：quick-ui 全局插件批次上报 + 管理端查询。
 * <p>
 * 上报接口需登录（Bearer + Client HMAC），但不校验菜单权限，避免普通用户无法埋点；
 * 查询/删除需 {@code monitor:clientTrack:list} / {@code monitor:clientTrack:remove}。
 */
@Tag(name = "前端行为监控")
@Validated
@RestController
@RequestMapping("/monitor/clientTrack")
@RequiredArgsConstructor
public class SysClientTrackController {

    private final SysClientTrackService clientTrackService;

    /**
     * 接收前端监控批次（本地缓冲 flush 后调用；静默场景下前端不解析响应体）。
     *
     * @param body    事件批次
     * @param request 用于记录客户端 IP
     * @return 空载荷成功
     */
    @Operation(summary = "前端监控批次上报")
    @IgnoreLogger(type = IgnoreLogger.Type.ALL)
    @PostMapping("/report")
    public R<Void> report(@Validated @RequestBody ClientTrackReportBo body, HttpServletRequest request) {
        StpUtil.checkLogin();
        clientTrackService.report(body, request);
        return R.ok();
    }

    @Operation(summary = "前端监控批次分页列表")
    @SaCheckPermission("monitor:clientTrack:list")
    @GetMapping("/list")
    public R<PageInfo<SysClientTrackVo>> list(@Validated SysClientTrackQueryBo query) {
        return R.ok(clientTrackService.page(query));
    }

    @Operation(summary = "删除前端监控批次（批量）")
    @SaCheckPermission("monitor:clientTrack:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> batchIds) {
        clientTrackService.removeBatch(batchIds);
        return R.ok();
    }
}
