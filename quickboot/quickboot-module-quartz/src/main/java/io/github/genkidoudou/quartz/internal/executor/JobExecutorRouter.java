package io.github.genkidoudou.quartz.internal.executor;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.quartz.internal.quartz.JobTaskSnapshot;
import io.github.genkidoudou.quartz.internal.support.JobType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 按 {@code jobType} 路由到具体 {@link JobExecutor}。
 */
@Component
public class JobExecutorRouter {

    private final List<JobExecutor> executors;

    public JobExecutorRouter(List<JobExecutor> executors) {
        this.executors = executors;
    }

    /**
     * 执行任务快照。
     */
    public void execute(JobTaskSnapshot snapshot) {
        JobType type = JobType.fromCode(snapshot.getJobType());
        if (type == null) {
            throw WarningException.literal(ErrorCodes.Job.JOB_TYPE_INVALID, "不支持的任务类型: " + snapshot.getJobType());
        }
        for (JobExecutor executor : executors) {
            if (executor.supports(type)) {
                executor.execute(snapshot);
                return;
            }
        }
        throw WarningException.literal(ErrorCodes.Job.JOB_TYPE_INVALID, "未找到任务执行器: " + type);
    }
}
