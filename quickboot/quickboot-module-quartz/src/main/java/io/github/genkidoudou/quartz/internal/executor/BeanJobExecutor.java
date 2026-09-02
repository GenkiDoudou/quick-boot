package io.github.genkidoudou.quartz.internal.executor;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.quartz.api.ITask;
import io.github.genkidoudou.quartz.internal.quartz.JobExecutionBridge;
import io.github.genkidoudou.quartz.internal.quartz.JobTaskSnapshot;
import io.github.genkidoudou.quartz.internal.support.JobType;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Bean 模式：按 {@code invoke_target} 调用 {@link ITask}。
 */
@Component
public class BeanJobExecutor implements JobExecutor {

    @Override
    public boolean supports(JobType jobType) {
        return jobType == JobType.BEAN;
    }

    @Override
    public void execute(JobTaskSnapshot snapshot) {
        ApplicationContext ctx = JobExecutionBridge.getApplicationContext();
        if (ctx == null) {
            throw WarningException.literal(ErrorCodes.System.DEPENDENCY_UNAVAILABLE, "Spring 容器未就绪，无法执行任务");
        }
        String target = snapshot.getInvokeTarget();
        Object bean;
        try {
            bean = ctx.getBean(target);
        } catch (Exception e) {
            throw WarningException.literal(ErrorCodes.Job.INVOKE_TARGET_NOT_FOUND, "调用目标 Bean 不存在: " + target);
        }
        if (!(bean instanceof ITask task)) {
            throw WarningException.literal(ErrorCodes.Job.INVOKE_TARGET_NOT_TASK, "调用目标必须实现 ITask: " + target);
        }
        String params = snapshot.getParams() == null ? "" : snapshot.getParams();
        task.execute(params);
    }
}
