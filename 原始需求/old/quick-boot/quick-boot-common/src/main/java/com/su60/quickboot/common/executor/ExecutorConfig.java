package com.su60.quickboot.common.executor;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@EnableAsync
public class ExecutorConfig implements AsyncConfigurer {
	@Bean
	public ExecutorService traceExecutorService(ContextSnapshotFactory factory) {
		ExecutorService delegate = Executors.newFixedThreadPool(10);
		return ContextExecutorService.wrap(
				delegate,
				factory::captureAll   // ✅ Supplier<ContextSnapshot>
		);
	}

	/**
	 * Micrometer 上下文快照工厂
	 */
	@Bean
	public ContextSnapshotFactory contextSnapshotFactory() {
		return ContextSnapshotFactory.builder().build();
	}

	/**
	 * Trace 上下文装饰器（关键）
	 */
	@Bean
	public TaskDecorator traceTaskDecorator(ContextSnapshotFactory factory) {
		return runnable -> {
			ContextSnapshot snapshot = factory.captureAll();
			return snapshot.wrap(runnable);
		};
	}

	/**
	 * 默认 Async Executor
	 * 只要你用 @Async()，就一定走这里
	 */
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("async-");
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(50);
		executor.setQueueCapacity(1000);
		executor.setTaskDecorator(traceTaskDecorator(contextSnapshotFactory()));
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (ex, method, params) ->
				System.err.println("Async error in " + method.getName() + ": " + ex.getMessage());
	}
}