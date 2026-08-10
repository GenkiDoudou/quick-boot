package io.github.genkidoudou.quartz.internal.quartz;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.quartz.internal.entity.SysJob;
import io.github.genkidoudou.quartz.internal.mapper.SysJobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 启动时对账 Quartz JDBC 元数据，清理 dev 热重启遗留的半删触发器，避免 MisfireHandler 反复报错。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzJobStoreReconciler {

    private static final String SQL_MANAGED_JOBS = """
        SELECT job_name, job_group
        FROM QRTZ_JOB_DETAILS
        WHERE sched_name = ?
          AND job_name LIKE ?
        """;

    private final JdbcTemplate jdbcTemplate;
    private final SysJobMapper jobMapper;

    /**
     * 早于 {@link JobScheduleRunner} 执行，在 Scheduler 延迟启动前清理 JDBC 脏数据。
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void reconcileOnStartup() {
        int orphanTriggers = QuartzJdbcJobStoreCleanup.cleanBrokenCronMetadata(
            jdbcTemplate, ScheduleUtils.SCHEDULER_NAME);
        int staleJobs = removeStaleManagedJobs();
        log.info("Quartz JobStore 对账完成：清理孤儿 CRON 触发器 {} 条，移除未登记任务 {} 条",
            orphanTriggers, staleJobs);
    }

    private int removeStaleManagedJobs() {
        Set<Long> registeredJobIds = loadRegisteredJobIds();
        List<Map<String, Object>> quartzJobs = jdbcTemplate.queryForList(
            SQL_MANAGED_JOBS, ScheduleUtils.SCHEDULER_NAME, ScheduleUtils.TASK_CLASS_NAME + "%");
        int removed = 0;
        for (Map<String, Object> row : quartzJobs) {
            String jobName = column(row, "job_name");
            String jobGroup = column(row, "job_group");
            Long jobId = JobTaskSnapshotSupport.parseJobId(JobKey.jobKey(jobName, jobGroup));
            if (jobId == null || registeredJobIds.contains(jobId)) {
                continue;
            }
            QuartzJdbcJobStoreCleanup.forceDeleteJob(
                jdbcTemplate, ScheduleUtils.SCHEDULER_NAME, JobKey.jobKey(jobName, jobGroup));
            removed++;
            log.warn("已移除 sys_job 中不存在的 Quartz 任务 {}.{}", jobGroup, jobName);
        }
        return removed;
    }

    private Set<Long> loadRegisteredJobIds() {
        List<SysJob> jobs = jobMapper.selectList(Wrappers.<SysJob>lambdaQuery().select(SysJob::getJobId));
        Set<Long> ids = new HashSet<>(jobs.size());
        for (SysJob job : jobs) {
            if (job.getJobId() != null) {
                ids.add(job.getJobId());
            }
        }
        return ids;
    }

    private static String column(Map<String, Object> row, String name) {
        Object value = row.get(name);
        if (value == null) {
            value = row.get(name.toUpperCase());
        }
        return value == null ? null : value.toString();
    }
}
