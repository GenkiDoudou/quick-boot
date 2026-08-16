package io.github.genkidoudou.quartz.internal.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 在任务加载完成后、Scheduler 延迟启动前再做一次 JDBC 对账。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzJobStorePostReconciler {

    private final JdbcTemplate jdbcTemplate;

    /** 任务加载完成后二次对账，清理孤儿 CRON 触发器。 */
    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE + 15)
    public void reconcileAfterJobLoad() {
        int cleaned = QuartzJdbcJobStoreCleanup.cleanBrokenCronMetadata(
            jdbcTemplate, ScheduleUtils.SCHEDULER_NAME);
        if (cleaned > 0) {
            log.info("Quartz 任务加载后二次对账：清理孤儿 CRON 触发器 {} 条", cleaned);
        }
    }
}
