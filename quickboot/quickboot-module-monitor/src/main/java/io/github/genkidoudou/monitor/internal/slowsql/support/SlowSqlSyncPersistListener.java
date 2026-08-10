package io.github.genkidoudou.monitor.internal.slowsql.support;

import io.github.genkidoudou.common.monitor.slowsql.SlowSqlCapturedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 同步写入 {@code sys_slow_sql}。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.monitor.slow-sql", name = "async-enabled", havingValue = "false")
public class SlowSqlSyncPersistListener {

    private final SlowSqlPersistSupport persistSupport;

    @EventListener
    public void onSlowSqlCaptured(SlowSqlCapturedEvent event) {
        persistSupport.persist(event);
    }
}
