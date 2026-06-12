package io.github.genkidoudou.web.knowledge.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/**
 * 文档异步入库线程池与并发控制（参考导入模块 {@code ImportAsyncConfiguration}）。
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(prefix = "qc.knowledge", name = "enabled", havingValue = "true")
public class IngestAsyncConfiguration {

    /**
     * 异步入库任务线程池。
     *
     * @param props 知识库配置
     * @return 线程池执行器
     */
    @Bean(name = "ingestTaskExecutor")
    public Executor ingestTaskExecutor(KnowledgeProperties props) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int max = Math.max(1, props.getIngest().getAsyncMaxConcurrent());
        executor.setCorePoolSize(max);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ingest-task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * 异步入库并发信号量，限制同时执行的入库流水线数量。
     *
     * @param props 知识库配置
     * @return 信号量
     */
    @Bean(name = "ingestTaskSemaphore")
    public Semaphore ingestTaskSemaphore(KnowledgeProperties props) {
        return new Semaphore(Math.max(1, props.getIngest().getAsyncMaxConcurrent()));
    }
}
