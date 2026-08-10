package io.github.genkidoudou.quartz.internal.task;

import io.github.genkidoudou.quartz.api.ITask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 示例定时任务 Bean，联调时可将 {@code invoke_target} 设为 {@code qcDemoTask}。
 */
@Slf4j
@Component("qcDemoTask")
public class QcDemoTask implements ITask {

    @Override
    public void execute(String params) {
        log.info("qcDemoTask 执行, params={}", params);
    }
}
