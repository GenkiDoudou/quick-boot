package io.github.genkidoudou.web.monitor.job.quartz;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 按 {@code invoke_target} 调用 {@link ITask} Bean。
 */
@Component
public class JobTaskInvoker {

    /**
     * 执行定时任务业务逻辑。
     *
     * @param snapshot 任务快照
     */
    public void invoke(JobTaskSnapshot snapshot) {
        ApplicationContext ctx = JobExecutionBridge.getApplicationContext();
        if (ctx == null) {
            throw new WarningException(ErrorCodes.System.DEPENDENCY_UNAVAILABLE, "Spring 容器未就绪，无法执行任务");
        }
        String target = snapshot.getInvokeTarget();
        Object bean;
        try {
            bean = ctx.getBean(target);
        } catch (Exception e) {
            throw new WarningException(ErrorCodes.Job.INVOKE_TARGET_NOT_FOUND, "调用目标 Bean 不存在: " + target);
        }
        if (!(bean instanceof ITask task)) {
            throw new WarningException(ErrorCodes.Job.INVOKE_TARGET_NOT_TASK, "调用目标必须实现 ITask: " + target);
        }
        String params = snapshot.getParams() == null ? "" : snapshot.getParams();
        task.execute(params);
    }
}
