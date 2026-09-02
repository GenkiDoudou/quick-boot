package io.github.genkidoudou.quartz.internal.executor;

import io.github.genkidoudou.quartz.internal.quartz.JobTaskSnapshot;
import io.github.genkidoudou.quartz.internal.support.JobType;

/**
 * 按任务类型执行定时任务。
 */
public interface JobExecutor {

    /** 是否支持该任务类型。 */
    boolean supports(JobType jobType);

    /** 执行任务逻辑。 */
    void execute(JobTaskSnapshot snapshot);
}
