package io.github.genkidoudou.web.system.exporttask.config;

import io.github.genkidoudou.common.exporttask.QcExportProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/**
 * 导出异步线程池与并发控制。
 */
@Configuration
public class ExportAsyncConfiguration {

    @Bean(name = "exportTaskExecutor")
    public Executor exportTaskExecutor(QcExportProperties props) {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        int max = Math.max(1, props.getAsyncMaxConcurrent());
        ex.setCorePoolSize(max);
        ex.setMaxPoolSize(max);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("export-task-");
        ex.initialize();
        return ex;
    }

    @Bean
    public Semaphore exportTaskSemaphore(QcExportProperties props) {
        return new Semaphore(Math.max(1, props.getAsyncMaxConcurrent()));
    }
}
