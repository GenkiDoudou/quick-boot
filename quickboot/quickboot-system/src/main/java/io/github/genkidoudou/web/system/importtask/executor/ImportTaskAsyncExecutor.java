package io.github.genkidoudou.web.system.importtask.executor;

import io.github.genkidoudou.web.system.importtask.domain.SysImportTask;
import io.github.genkidoudou.web.system.importtask.mapper.SysImportTaskMapper;
import io.github.genkidoudou.web.system.importtask.service.ImportOrchestratorService;
import io.github.genkidoudou.web.system.importtask.support.ImportTaskStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;

/**
 * 异步执行导入任务（等价于 IMPORT_TASK 后台 Job）。
 */
@Component
public class ImportTaskAsyncExecutor {

    private final ImportOrchestratorService orchestratorService;
    private final SysImportTaskMapper taskMapper;
    private final Semaphore importTaskSemaphore;

    public ImportTaskAsyncExecutor(ImportOrchestratorService orchestratorService,
                                   SysImportTaskMapper taskMapper,
                                   Semaphore importTaskSemaphore) {
        this.orchestratorService = orchestratorService;
        this.taskMapper = taskMapper;
        this.importTaskSemaphore = importTaskSemaphore;
    }

    @Async("importTaskExecutor")
    public void runAsync(Long taskId) {
        if (!importTaskSemaphore.tryAcquire()) {
            SysImportTask task = taskMapper.selectById(taskId);
            if (task != null) {
                task.setStatus(ImportTaskStatus.FAILED);
                task.setErrorMessage("导入队列繁忙，请稍后重试");
                task.setFinishTime(LocalDateTime.now());
                taskMapper.updateById(task);
            }
            return;
        }
        try {
            orchestratorService.executeAsyncTask(taskId);
        } finally {
            importTaskSemaphore.release();
        }
    }
}
