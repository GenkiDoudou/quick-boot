package io.github.genkidoudou.monitor.internal.tracechain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.monitor.internal.tracechain.dto.TraceChainGraphVo;
import io.github.genkidoudou.monitor.internal.tracechain.dto.TraceChainQueryBo;
import io.github.genkidoudou.monitor.internal.tracechain.service.SysTraceChainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全链路监控：聚合前端行为、页面跳转、操作日志与慢 SQL。
 */
@Tag(name = "全链路监控")
@Validated
@RestController
@RequestMapping("/monitor/traceChain")
@RequiredArgsConstructor
@IgnoreLogger(type = IgnoreLogger.Type.ALL)
public class SysTraceChainController {

    private final SysTraceChainService traceChainService;

    @Operation(summary = "全链路图（页面跳转 + 行为明细 + 后端资源）")
    @SaCheckPermission("monitor:traceChain:query")
    @GetMapping("/graph")
    public R<TraceChainGraphVo> graph(@Validated TraceChainQueryBo query) {
        return R.ok(traceChainService.graph(query));
    }
}
