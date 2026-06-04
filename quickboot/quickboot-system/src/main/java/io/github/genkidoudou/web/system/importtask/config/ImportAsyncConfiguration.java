package io.github.genkidoudou.web.system.importtask.config;

import io.github.genkidoudou.common.importtask.QcImportProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/**
 * 导入异步线程池与并发控制。
 */
@Configuration
@EnableAsync
public class ImportAsyncConfiguration {

    @Bean(name = "importTaskExecutor")
    public Executor importTaskExecutor(QcImportProperties props) {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        int max = Math.max(1, props.getAsyncMaxConcurrent());
        ex.setCorePoolSize(max);
        ex.setMaxPoolSize(max);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("import-task-");
        ex.initialize();
        return ex;
    }

    @Bean
    public Semaphore importTaskSemaphore(QcImportProperties props) {
        return new Semaphore(Math.max(1, props.getAsyncMaxConcurrent()));
    }
}
