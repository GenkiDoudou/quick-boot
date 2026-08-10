package io.github.genkidoudou.quartz.api;

/**
 * 定时任务业务执行契约：由 {@code invoke_target} 指定的 Spring Bean 实现。
 */
public interface ITask {

    /**
     * 执行任务逻辑。
     *
     * @param params 任务参数，来自 {@code sys_job.params}，可为空串
     */
    void execute(String params);
}
