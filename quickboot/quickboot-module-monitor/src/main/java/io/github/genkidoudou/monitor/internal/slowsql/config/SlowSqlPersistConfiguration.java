package io.github.genkidoudou.monitor.internal.slowsql.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 慢 SQL 异步落库线程池。
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(prefix = "qc.monitor.slow-sql", name = "async-enabled", havingValue = "true", matchIfMissing = true)
public class SlowSqlPersistConfiguration {

    /**
     * 慢 SQL 异步落库专用线程池。
     *
     * @return 线程池执行器
     */
    @Bean(name = "slowSqlTaskExecutor")
    public Executor slowSqlTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(512);
        executor.setThreadNamePrefix("slow-sql-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
