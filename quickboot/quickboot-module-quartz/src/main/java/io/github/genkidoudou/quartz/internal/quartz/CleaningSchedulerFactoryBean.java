package io.github.genkidoudou.quartz.internal.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 在 Quartz {@link Scheduler#start()} 之前再次清理 JDBC 脏数据，避免延迟启动窗口内残留半删触发器。
 */
@Slf4j
class CleaningSchedulerFactoryBean extends SchedulerFactoryBean {

    private final JdbcTemplate jdbcTemplate;

    CleaningSchedulerFactoryBean(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void startScheduler(Scheduler scheduler, int startupDelay) throws SchedulerException {
        if (startupDelay <= 0) {
            cleanupBeforeSchedulerStart();
            scheduler.start();
            return;
        }
        Timer timer = new Timer("Quartz Scheduler Timer-" + scheduler.getSchedulerName(), true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    cleanupBeforeSchedulerStart();
                    scheduler.start();
                } catch (SchedulerException ex) {
                    log.error("Quartz Scheduler 启动失败", ex);
                }
            }
        }, startupDelay * 1000L);
    }

    private void cleanupBeforeSchedulerStart() {
        try {
            QuartzJdbcJobStoreCleanup.cleanBrokenCronMetadata(jdbcTemplate, ScheduleUtils.SCHEDULER_NAME);
        } catch (Exception ex) {
            log.warn("Scheduler 启动前 JDBC 对账失败: {}", ex.getMessage());
        }
    }
}
