package io.github.genkidoudou.monitor.internal.litetrace.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.monitor.slowsql.SlowSqlCapturePayload;
import io.github.genkidoudou.common.monitor.slowsql.SlowSqlCapturedEvent;
import io.github.genkidoudou.monitor.internal.litetrace.service.TraceProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 复用慢 SQL 采集事件：有 {@code traceId} 时投影 {@code sql} span。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LiteTraceSlowSqlListener {

    private final TraceProjectionService projectionService;

    @Value("${qc.lite-trace.sql-project-enabled:true}")
    private boolean enabled;

    /**
     * 监听慢 SQL 采集事件，有 traceId 时投影 sql span（跳过监控表自身 SQL）。
     *
     * @param event 慢 SQL 采集事件
     */
    @EventListener
    public void onSlowSql(SlowSqlCapturedEvent event) {
        if (!enabled || event == null || event.getPayload() == null) {
            return;
        }
        SlowSqlCapturePayload p = event.getPayload();
        if (StrUtil.isBlank(p.getTraceId())) {
            return;
        }
        String sql = StrUtil.blankToDefault(p.getSqlText(), "");
        if (sql.contains("sys_trace_") || sql.contains("sys_rum_event") || sql.contains("sys_slow_sql")) {
            return;
        }
        try {
            projectionService.projectSql(p.getTraceId(), sql, p.getCostTimeMs(), p.getMapperId());
        } catch (Exception ex) {
            log.warn("lite-trace sql project failed: {}", ex.getMessage());
        }
    }
}
