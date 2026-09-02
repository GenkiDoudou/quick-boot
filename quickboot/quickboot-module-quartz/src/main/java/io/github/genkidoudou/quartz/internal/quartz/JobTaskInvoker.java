package io.github.genkidoudou.quartz.internal.quartz;

import io.github.genkidoudou.quartz.internal.executor.JobExecutorRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 定时任务统一调用入口（委托 {@link JobExecutorRouter} 按类型执行）。
 */
@Component
@RequiredArgsConstructor
public class JobTaskInvoker {

    private final JobExecutorRouter executorRouter;

    /**
     * 执行定时任务业务逻辑。
     *
     * @param snapshot 任务快照
     */
    public void invoke(JobTaskSnapshot snapshot) {
        executorRouter.execute(snapshot);
    }
}
