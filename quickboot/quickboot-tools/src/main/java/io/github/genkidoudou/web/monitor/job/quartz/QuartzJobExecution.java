package io.github.genkidoudou.web.monitor.job.quartz;

import org.quartz.JobExecutionContext;

/**
 * 允许并发执行的 Quartz Job。
 */
public class QuartzJobExecution extends AbstractQuartzJob {

    @Override
    protected void doExecute(JobExecutionContext context, JobTaskSnapshot snapshot) {
        JobTaskInvoker invoker = JobExecutionBridge.getJobTaskInvoker();
        if (invoker == null) {
            throw new IllegalStateException("JobTaskInvoker 未初始化");
        }
        invoker.invoke(snapshot);
    }
}
