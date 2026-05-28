package io.github.genkidoudou.web.monitor.job.quartz;

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

    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return TriggerKey.triggerKey(TASK_CLASS_NAME + jobId, jobGroup);
    }

    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return JobKey.jobKey(TASK_CLASS_NAME + jobId, jobGroup);
    }

    /**
     * 创建或覆盖调度任务；若 status=1 则创建后暂停。
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
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
        if (CronUtils.getNextExecution(cron) != null) {
            scheduler.scheduleJob(jobDetail, trigger);
        }
        if ("1".equals(snapshot.getStatus()) && scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);
        }
    }

    public static void updateScheduleJob(Scheduler scheduler, JobTaskSnapshot snapshot) throws SchedulerException {
        JobKey jobKey = getJobKey(snapshot.getJobId(), snapshot.getJobGroup());
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
        createScheduleJob(scheduler, snapshot);
    }

    private static CronScheduleBuilder applyMisfirePolicy(String policy, CronScheduleBuilder cb) {
        return switch (policy == null ? MISFIRE_DEFAULT : policy) {
            case MISFIRE_DEFAULT -> cb;
            case MISFIRE_IGNORE_MISFIRES -> cb.withMisfireHandlingInstructionIgnoreMisfires();
            case MISFIRE_FIRE_AND_PROCEED -> cb.withMisfireHandlingInstructionFireAndProceed();
            case MISFIRE_DO_NOTHING -> cb.withMisfireHandlingInstructionDoNothing();
            default -> throw new WarningException(
                ErrorCodes.Job.CRON_INVALID,
                "错失策略无效: " + policy
            );
        };
    }
}
