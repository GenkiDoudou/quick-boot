package com.su60.quickboot.quartz.utils;

import com.su60.quickboot.data.spring.SpringContextHolder;
import com.su60.quickboot.quartz.dos.SysJobDo;
import org.quartz.JobExecutionContext;

/**
 * 定时任务处理（允许并发执行）
 *
 * @author luyanan
 * @since 2024/11/11
 */
public class QuartzJobExecution extends AbstractQuartzJob {
	@Override
	protected void doExecute(JobExecutionContext context, SysJobDo sysJob) throws Exception {

		Object bean = SpringContextHolder.getBean(sysJob.getInvokeTarget());
		if (bean instanceof ITask task) {
			task.execute(sysJob.getParams());
		}
	}
}