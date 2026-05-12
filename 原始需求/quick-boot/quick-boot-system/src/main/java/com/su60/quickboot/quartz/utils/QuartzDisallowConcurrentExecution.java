package com.su60.quickboot.quartz.utils;

import com.su60.quickboot.data.spring.SpringContextHolder;
import com.su60.quickboot.quartz.dos.SysJobDo;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * 定时任务处理（禁止并发执行）
 *
 * @author luyanan
 * @since 2024/11/11
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob {

	/**
	 * 执行任务
	 *
	 * @param context  上下文
	 * @param sysJobDo 任务
	 * @return
	 * @since 2024/11/12
	 */
	@Override
	protected void doExecute(JobExecutionContext context, SysJobDo sysJobDo) {
		Object bean = SpringContextHolder.getBean(sysJobDo.getInvokeTarget());
		if (bean instanceof ITask iTask) {
			iTask.execute(sysJobDo.getParams());
		}
	}
}
