package com.su60.quickboot.common.executor;

import io.micrometer.context.ContextSnapshot;
import org.springframework.core.task.TaskDecorator;

public class TraceTaskDecorator implements TaskDecorator {

	@Override
	public Runnable decorate(Runnable runnable) {
		ContextSnapshot snapshot = ContextSnapshot.captureAll();
		return () -> {
			try (ContextSnapshot.Scope scope = snapshot.setThreadLocals()) {
				runnable.run();
			}
		};
	}
}