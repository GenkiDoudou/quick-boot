package io.github.genkidoudou.web.system.exporttask.executor;

import io.github.genkidoudou.web.system.exporttask.domain.SysExportTask;
import io.github.genkidoudou.web.system.exporttask.mapper.SysExportTaskMapper;
import io.github.genkidoudou.web.system.exporttask.service.ExportOrchestratorService;
import io.github.genkidoudou.web.system.exporttask.support.ExportTaskStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;

/**
 * 异步执行导出任务。
 */
@Component
public class ExportTaskAsyncExecutor {

    private final ExportOrchestratorService orchestratorService;
    private final SysExportTaskMapper taskMapper;
    private final Semaphore exportTaskSemaphore;

    public ExportTaskAsyncExecutor(ExportOrchestratorService orchestratorService,
                                    SysExportTaskMapper taskMapper,
                                    Semaphore exportTaskSemaphore) {
        this.orchestratorService = orchestratorService;
        this.taskMapper = taskMapper;
        this.exportTaskSemaphore = exportTaskSemaphore;
    }

    @Async("exportTaskExecutor")
    public void runAsync(Long taskId) {
        if (!exportTaskSemaphore.tryAcquire()) {
            SysExportTask task = taskMapper.selectById(taskId);
            if (task != null) {
                task.setStatus(ExportTaskStatus.FAILED);
                task.setErrorMessage("导出队列繁忙，请稍后重试");
                task.setFinishTime(LocalDateTime.now());
                taskMapper.updateById(task);
            }
            return;
        }
        try {
            orchestratorService.executeAsyncTask(taskId);
        } finally {
            exportTaskSemaphore.release();
        }
    }
}
