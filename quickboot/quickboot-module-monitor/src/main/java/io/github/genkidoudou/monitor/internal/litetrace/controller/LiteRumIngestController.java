package io.github.genkidoudou.monitor.internal.litetrace.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.monitor.internal.litetrace.dto.RumIngestBo;
import io.github.genkidoudou.monitor.internal.litetrace.service.LiteRumIngestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lite RUM 上报（需登录，不校验菜单权限）。
 */
@Tag(name = "Lite链路RUM上报")
@Validated
@RestController
@RequestMapping("/monitor/liteTrace")
@RequiredArgsConstructor
public class LiteRumIngestController {

    private final LiteRumIngestService liteRumIngestService;

    /**
     * 接收前端 RUM 批量事件并落库、投影链路索引。
     *
     * @param body    上报体，含 appId 与 events 列表
     * @param request 用于提取客户端 IP、User-Agent
     * @return 空成功响应；校验失败或限流时抛业务异常
     */
    @Operation(summary = "RUM 批量上报")
    @IgnoreLogger(type = IgnoreLogger.Type.ALL)
    @PostMapping("/rum/ingest")
    public R<Void> ingest(@Validated @RequestBody RumIngestBo body, HttpServletRequest request) {
        StpUtil.checkLogin();
        liteRumIngestService.ingest(body, request);
        return R.ok();
    }
}
