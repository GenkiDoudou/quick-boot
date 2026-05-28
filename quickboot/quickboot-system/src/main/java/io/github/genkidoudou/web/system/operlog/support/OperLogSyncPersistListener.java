package io.github.genkidoudou.web.system.operlog.support;

import io.github.genkidoudou.common.monitor.operlog.OperLogCapturedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 同步监听 {@link OperLogCapturedEvent} 并写入 {@code sys_oper_log}。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.monitor.operlog", name = "async-enabled", havingValue = "false")
public class OperLogSyncPersistListener {

    private final OperLogPersistSupport persistSupport;

    @EventListener
    public void onOperLogCaptured(OperLogCapturedEvent event) {
        persistSupport.persist(event);
    }
}
