package io.github.genkidoudou.web.workflow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/**
 * 工作流异步运行线程池与并发控制（参考 {@code IngestAsyncConfiguration}）。
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(prefix = "qc.workflow", name = "enabled", havingValue = "true")
public class WorkflowAsyncConfiguration {

    /**
     * 异步工作流运行线程池。
     *
     * @param props 工作流配置
     * @return 线程池执行器
     */
    @Bean(name = "workflowRunExecutor")
    public Executor workflowRunExecutor(WorkflowProperties props) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int max = Math.max(1, props.getMaxConcurrentRunsPerUser());
        executor.setCorePoolSize(max);
        executor.setMaxPoolSize(max * 2);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("workflow-run-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * 全局异步运行并发信号量。
     *
     * @param props 工作流配置
     * @return 信号量
     */
    @Bean(name = "workflowRunSemaphore")
    public Semaphore workflowRunSemaphore(WorkflowProperties props) {
        return new Semaphore(Math.max(1, props.getMaxConcurrentRunsPerUser() * 2));
    }
}
