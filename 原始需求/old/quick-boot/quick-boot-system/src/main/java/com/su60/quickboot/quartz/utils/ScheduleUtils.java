package com.su60.quickboot.quartz.utils;

import cn.hutool.core.util.ObjectUtil;
import com.su60.quickboot.quartz.dos.SysJobDo;
import com.su60.quickboot.quartz.exception.TaskException;
import lombok.experimental.UtilityClass;
import org.quartz.*;

import static com.su60.quickboot.quartz.utils.AbstractQuartzJob.TASK_PROPERTIES;

/**
 * 定时任务工具类
 *
 * @author luyanan
 * @since 2024/11/11
 */
@UtilityClass
public class ScheduleUtils {

	public static final String TASK_CLASS_NAME = "TASK_CLASS_NAME";

	/**
	 * 默认
	 */
	public static final String MISFIRE_DEFAULT = "0";

	/**
	 * 立即触发执行
	 */
	public static final String MISFIRE_IGNORE_MISFIRES = "1";

	/**
	 * 触发一次执行
	 */
	public static final String MISFIRE_FIRE_AND_PROCEED = "2";

	/**
	 * 不触发立即执行
	 */
	public static final String MISFIRE_DO_NOTHING = "3";

	/**
	 * 获取quartz任务类
	 *
	 * @param sysJobDo 执行计划
	 * @return 任务类
	 * @since 2024/11/11
	 */
	public Class<? extends Job> getQuartzJobClass(SysJobDo sysJobDo) {
		boolean isConcurrent = "Y".equals(sysJobDo.getConcurrent());
		return isConcurrent ? QuartzJobExecution.class : QuartzDisallowConcurrentExecution.class;
	}

	/**
	 * 构建任务触发对象
	 *
	 * @param jobId    任务id
	 * @param jobGroup 任务分组
	 * @return 任务触发对象
	 * @since 2024/11/11
	 */
	public TriggerKey getTriggerKey(Long jobId, String jobGroup) {
		return TriggerKey.triggerKey(TASK_CLASS_NAME + jobId, jobGroup);
	}

	/**
	 * 构建任务键对象
	 *
	 * @param jobId    任务ud
	 * @param jobGroup 任务分组
	 * @return 任务键对象
	 * @since 2024/11/11
	 */
	public JobKey getJobKey(Long jobId, String jobGroup) {

		return JobKey.jobKey(TASK_CLASS_NAME + jobId, jobGroup);
	}

	/**
	 * 创建定时任务
	 *
	 * @param scheduler 执行器
	 * @param sysJobDo  任务信息
	 * @since 2024/11/11
	 */
	public void createScheduleJob(Scheduler scheduler, SysJobDo sysJobDo) throws SchedulerException {
		Class<? extends Job> quartzJobClass =
				getQuartzJobClass(sysJobDo);
		// 构建Job信息
		Long jobId = sysJobDo.getId();
		String jobGroup = sysJobDo.getJobGroup();
		JobDetail jobDetail = JobBuilder.newJob(quartzJobClass).withIdentity(getJobKey(jobId, jobGroup)).build();
		// 表达式调度构建器
		CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(sysJobDo.getCronExpression());
		cronScheduleBuilder = handleCronScheduleMisfirePolicy(sysJobDo, cronScheduleBuilder);

		// 按新的cronExpression表达式构建一个新的trigger
		CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerKey(jobId, jobGroup))
				.withSchedule(cronScheduleBuilder).build();

		// 放入参数，运行时的方法可以获取
		jobDetail.getJobDataMap().put(TASK_PROPERTIES, sysJobDo);

		// 判断是否存在
		if (scheduler.checkExists(getJobKey(jobId, jobGroup))) {
			// 防止创建时存在数据问题 先移除，然后在执行创建操作
			scheduler.deleteJob(getJobKey(jobId, jobGroup));
		}

		// 判断任务是否过期
		if (ObjectUtil.isNotNull(CronUtils.getNextExecution(sysJobDo.getCronExpression()))) {
			// 执行调度任务
			scheduler.scheduleJob(jobDetail, trigger);
		}

		// 暂停任务
		if (sysJobDo.getStatus().equals("1")) {
			scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
		}

	}


	/**
	 * 设置定时任务策略
	 *
	 * @param job 任务
	 * @param cb  CronScheduleBuilder
	 * @return 定时任务策略
	 * @since 2024/11/12
	 */
	private CronScheduleBuilder handleCronScheduleMisfirePolicy(SysJobDo job, CronScheduleBuilder cb)
			throws TaskException {
		return switch (job.getMisfirePolicy()) {
			case MISFIRE_DEFAULT -> cb;
			case MISFIRE_IGNORE_MISFIRES -> cb.withMisfireHandlingInstructionIgnoreMisfires();
			case MISFIRE_FIRE_AND_PROCEED -> cb.withMisfireHandlingInstructionFireAndProceed();
			case MISFIRE_DO_NOTHING -> cb.withMisfireHandlingInstructionDoNothing();
			default ->
					throw new TaskException(TaskException.Code.CONFIG_ERROR, "The task misfire policy '" + job.getMisfirePolicy()
							+ "' cannot be used in cron schedule tasks");
		};
	}

	/**
	 * 修改定时任务
	 *
	 * @param scheduler 执行器
	 * @param sysJobDo  任务信息
	 * @return
	 * @since 2024/11/12
	 */
	public void updateScheduleJob(Scheduler scheduler, SysJobDo sysJobDo) throws SchedulerException {
		Long jobId =
				sysJobDo.getId();
		String jobGroup = sysJobDo.getJobGroup();
		// 判断是否存在
		JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
		if (scheduler.checkExists(jobKey)) {
			// 防止创建时存在数据问题 先移除，然后在执行创建操作
			scheduler.deleteJob(jobKey);
		}
		createScheduleJob(scheduler, sysJobDo);
	}
}
