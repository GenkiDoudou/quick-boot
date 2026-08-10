package io.github.genkidoudou.quartz.internal.quartz;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.quartz.internal.entity.SysJob;
import io.github.genkidoudou.quartz.internal.mapper.SysJobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用就绪且 Quartz Scheduler 已启动后，加载 {@code sys_job} 中启用的定时任务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobScheduleRunner {

    private final Scheduler scheduler;
    private final SysJobMapper jobMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 在对账 {@link QuartzJobStoreReconciler} 之后执行，避免与脏元数据竞态。
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public void loadEnabledJobsOnReady() {
        List<SysJob> jobs = jobMapper.selectList(
            Wrappers.<SysJob>lambdaQuery().eq(SysJob::getStatus, "0")
        );
        for (SysJob job : jobs) {
            try {
                ScheduleUtils.createScheduleJob(scheduler, JobTaskSnapshot.from(job));
            } catch (SchedulerException e) {
                log.error("加载定时任务失败 jobId={}", job.getJobId(), e);
            }
        }
        QuartzJdbcJobStoreCleanup.cleanBrokenCronMetadata(jdbcTemplate, ScheduleUtils.SCHEDULER_NAME);
        log.info("定时任务加载完成，共 {} 条启用任务", jobs.size());
    }
}
