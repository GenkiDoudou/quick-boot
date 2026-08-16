package io.github.genkidoudou.quartz.internal.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 运行时周期性清理 Quartz JDBC 半删触发器，避免 MisfireHandler 长期刷 ERROR。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzJobStoreMaintenance {

    private final JdbcTemplate jdbcTemplate;

    /** 周期性清理 QRTZ 半删触发器，避免 MisfireHandler 持续报错。 */
    @Scheduled(fixedDelayString = "${qc.monitor.job.store-maintenance-interval-ms:60000}")
    public void cleanBrokenMetadata() {
        int cleaned = QuartzJdbcJobStoreCleanup.cleanBrokenCronMetadata(
            jdbcTemplate, ScheduleUtils.SCHEDULER_NAME);
        if (cleaned > 0) {
            log.info("Quartz 运行时维护：清理孤儿 CRON 触发器 {} 条", cleaned);
        }
    }
}
