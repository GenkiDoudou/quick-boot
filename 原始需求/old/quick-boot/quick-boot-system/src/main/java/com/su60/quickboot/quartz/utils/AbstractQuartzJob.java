package com.su60.quickboot.quartz.utils;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.data.spring.SpringContextHolder;
import com.su60.quickboot.quartz.dos.SysJobDo;
import com.su60.quickboot.quartz.dos.SysJobLogDo;
import com.su60.quickboot.quartz.service.ISysJobLogService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

/**
 * quartz抽象类
 *
 * @author luyanan
 * @since 2024/11/11
 */
@Slf4j
public abstract class AbstractQuartzJob implements Job {
	/**
	 * 本地线程变量
	 *
	 * @since 2024/11/12
	 */

	private static ThreadLocal<Date> threadLocal = new ThreadLocal<>();
	/**
	 * 执行目标key
	 */
	public static final String TASK_PROPERTIES = "TASK_PROPERTIES";

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		SysJobDo sysJobDo = new SysJobDo();
		BeanConvertUtils.copyProperties(jobExecutionContext.getMergedJobDataMap().get(TASK_PROPERTIES), sysJobDo);


		try {
			before(jobExecutionContext, sysJobDo);
			if (null != sysJobDo) {
				doExecute(jobExecutionContext, sysJobDo);
			}
			after(jobExecutionContext, sysJobDo, null);
		} catch (Exception e) {
			log.error("任务执行异常  - ：", e);
			after(jobExecutionContext, sysJobDo, e);
		}

	}

	/**
	 * 执行前
	 *
	 * @param context  上下文
	 * @param sysJobDo 任务
	 * @since 2024/11/11
	 */
	protected void before(JobExecutionContext context, SysJobDo sysJobDo) {
		threadLocal.set(new Date());
	}

	/**
	 * 执行后
	 *
	 * @param context  上下文
	 * @param sysJobDo 任务
	 * @param e        异常信息
	 * @since 2024/11/11
	 */
	protected void after(JobExecutionContext context, SysJobDo sysJobDo, Exception e) {
		Date startTime = threadLocal.get();
		threadLocal.remove();
		final SysJobLogDo sysJobLog = new SysJobLogDo();
		sysJobLog.setJobId(sysJobDo.getId());
		sysJobLog.setJobName(sysJobDo.getJobName());
		sysJobLog.setJobGroup(sysJobDo.getJobGroup());
		sysJobLog.setInvokeTarget(sysJobDo.getInvokeTarget());
		sysJobLog.setStartTime(startTime);
		sysJobLog.setEndTime(new Date());
		sysJobLog.setParams(sysJobDo.getParams());
		long runMs = sysJobLog.getEndTime().getTime() - sysJobLog.getStartTime().getTime();
		sysJobLog.setTimeConsuming(runMs);
		if (e != null) {
			sysJobLog.setStatus("1");
			String errorMsg = StrUtil.sub(ExceptionUtil.getMessage(e), 0, 2000);
			sysJobLog.setExceptionInfo(errorMsg);
		} else {
			sysJobLog.setStatus("0");
		}

		// 写入数据库当中
		SpringContextHolder.getBean(ISysJobLogService.class).addJobLog(sysJobLog);

	}

	/**
	 * 执行方法.由子类实现
	 *
	 * @param context  上下文
	 * @param sysJobDo 任务
	 * @since 2024/11/11
	 */
	protected abstract void doExecute(JobExecutionContext context, SysJobDo sysJobDo) throws Exception;

}
