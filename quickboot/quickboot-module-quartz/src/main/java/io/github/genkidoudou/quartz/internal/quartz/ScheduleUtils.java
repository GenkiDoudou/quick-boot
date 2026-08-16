package io.github.genkidoudou.quartz.internal.quartz;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 * Quartz 调度注册工具。
 */
public final class ScheduleUtils {

    /** 与 {@link ScheduleConfig} 中 {@code org.quartz.scheduler.instanceName} 一致。 */
    public static final String SCHEDULER_NAME = "QuickScheduler";

    public static final String TASK_CLASS_NAME = "TASK_CLASS_NAME_";

    public static final String MISFIRE_DEFAULT = "0";
    public static final String MISFIRE_IGNORE_MISFIRES = "1";
    public static final String MISFIRE_FIRE_AND_PROCEED = "2";
    public static final String MISFIRE_DO_NOTHING = "3";

    private ScheduleUtils() {
    }

    /**
     * {@code concurrent=0} 允许并发；{@code 1} 禁止并发。
     */
    public static Class<? extends Job> getQuartzJobClass(String concurrent) {
        boolean allowConcurrent = "0".equals(concurrent);
        return allowConcurrent ? QuartzJobExecution.class : QuartzDisallowConcurrentExecution.class;
    }

    /** 构建 Quartz TriggerKey（与 Job 一一对应）。 */
    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return TriggerKey.triggerKey(TASK_CLASS_NAME + jobId, jobGroup);
    }

    /** 构建 Quartz JobKey。 */
    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return JobKey.jobKey(TASK_CLASS_NAME + jobId, jobGroup);
    }

    /**
     * 删除 Quartz 任务；若 Scheduler API 因 JDBC 脏数据失败则回退直接删表。
     */
    public static void deleteJobSafely(Scheduler scheduler, JobKey jobKey) throws SchedulerException {
        QuartzJdbcJobStoreCleanup.deleteJobSafely(scheduler, JobExecutionBridge.getJdbcTemplate(), jobKey);
    }

    /**
     * 创建或覆盖调度任务；{@code status=1}（暂停）时只清理、不注册到 JobStore。
     */
    public static void createScheduleJob(Scheduler scheduler, JobTaskSnapshot snapshot) throws SchedulerException {
        Long jobId = snapshot.getJobId();
        if (jobId == null) {
            throw new SchedulerException("jobId is null");
        }
        String jobGroup = snapshot.getJobGroup();
        String cron = snapshot.getCronExpression() == null ? "" : snapshot.getCronExpression().trim();
        Class<? extends Job> jobClass = getQuartzJobClass(snapshot.getConcurrent());
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(getJobKey(jobId, jobGroup)).build();

        CronScheduleBuilder cronBuilder = CronScheduleBuilder.cronSchedule(cron);
        cronBuilder = applyMisfirePolicy(snapshot.getMisfirePolicy(), cronBuilder);

        CronTrigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(getTriggerKey(jobId, jobGroup))
            .withSchedule(cronBuilder)
            .build();

        JobTaskSnapshotSupport.putTo(jobDetail.getJobDataMap(), snapshot);

        JobKey jobKey = getJobKey(jobId, jobGroup);
        deleteJobSafely(scheduler, jobKey);
        // 暂停任务不写入 JobStore：新增默认暂停；同事务内 schedule 后再 pauseJob
        // 会因 JobStoreCMT 非托管连接读不到未提交的 QRTZ_CRON_TRIGGERS 而失败。
        if ("1".equals(snapshot.getStatus())) {
            return;
        }
        if (CronUtils.getNextExecution(cron) != null) {
            scheduler.scheduleJob(jobDetail, trigger);
        }
    }

    /** 先删后建，刷新 Cron 与 JobDataMap。 */
    public static void updateScheduleJob(Scheduler scheduler, JobTaskSnapshot snapshot) throws SchedulerException {
        JobKey jobKey = getJobKey(snapshot.getJobId(), snapshot.getJobGroup());
        deleteJobSafely(scheduler, jobKey);
        createScheduleJob(scheduler, snapshot);
    }

    private static CronScheduleBuilder applyMisfirePolicy(String policy, CronScheduleBuilder cb) {
        return switch (policy == null ? MISFIRE_DEFAULT : policy) {
            case MISFIRE_DEFAULT -> cb;
            case MISFIRE_IGNORE_MISFIRES -> cb.withMisfireHandlingInstructionIgnoreMisfires();
            case MISFIRE_FIRE_AND_PROCEED -> cb.withMisfireHandlingInstructionFireAndProceed();
            case MISFIRE_DO_NOTHING -> cb.withMisfireHandlingInstructionDoNothing();
            default -> throw WarningException.literal(
                ErrorCodes.Job.CRON_INVALID,
                "错失策略无效: " + policy
            );
        };
    }
}
