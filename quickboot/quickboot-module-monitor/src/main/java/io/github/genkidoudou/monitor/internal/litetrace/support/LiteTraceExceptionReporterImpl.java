package io.github.genkidoudou.monitor.internal.litetrace.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.TraceIds;
import io.github.genkidoudou.common.monitor.ExceptionReporter;
import io.github.genkidoudou.monitor.internal.litetrace.service.TraceProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 将未处理异常投影为 {@code be_error} span。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LiteTraceExceptionReporterImpl implements ExceptionReporter {

    private final TraceProjectionService projectionService;

    @Value("${qc.lite-trace.exception-project-enabled:true}")
    private boolean enabled;

    @Override
    public void report(Throwable ex) {
        if (!enabled || ex == null) {
            return;
        }
        String tid = TraceIds.current();
        if (StrUtil.isBlank(tid)) {
            return;
        }
        try {
            String summary = ex.getClass().getSimpleName()
                + (StrUtil.isNotBlank(ex.getMessage()) ? (": " + ex.getMessage()) : "");
            String stack = stackSnippet(ex);
            projectionService.projectBeError(tid, summary, stack);
        } catch (Exception e) {
            log.warn("lite-trace be_error project failed: {}", e.getMessage());
        }
    }

    private static String stackSnippet(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] els = ex.getStackTrace();
        int n = Math.min(els == null ? 0 : els.length, 8);
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(els[i].toString());
        }
        return sb.toString();
    }
}
