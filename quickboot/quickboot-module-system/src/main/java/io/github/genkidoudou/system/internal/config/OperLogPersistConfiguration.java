package io.github.genkidoudou.system.internal.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 操作日志异步落库线程池（{@code qc.monitor.operlog.async-enabled=true} 时生效）。
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(prefix = "qc.monitor.operlog", name = "async-enabled", havingValue = "true", matchIfMissing = true)
public class OperLogPersistConfiguration {

  /**
   * 专用于操作日志持久化，避免占用公共 ForkJoin 池。
   *
   * @return 线程池执行器
   */
  @Bean(name = "operLogTaskExecutor")
  public Executor operLogTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(512);
    executor.setThreadNamePrefix("oper-log-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    executor.initialize();
    return executor;
  }
}
