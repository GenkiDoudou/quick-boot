package io.github.genkidoudou.web.knowledge.ingest;

import io.github.genkidoudou.web.knowledge.constants.KbTaskStatus;
import io.github.genkidoudou.web.knowledge.domain.KbIngestTask;
import io.github.genkidoudou.web.knowledge.mapper.KbIngestTaskMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;

/**
 * 异步入库任务执行器（参考 {@code ImportTaskAsyncExecutor}：独立线程池 + Semaphore 限流）。
 * <p>
 * 线程池 Bean 名为 {@code ingestTaskExecutor}，与本类 Bean 名 {@code ingestTaskAsyncExecutor} 区分，避免冲突。
 */
@Component
public class IngestTaskAsyncExecutor {

    private final DocumentIngestionService ingestionService;
    private final KbIngestTaskMapper taskMapper;
    private final Semaphore ingestTaskSemaphore;

    public IngestTaskAsyncExecutor(DocumentIngestionService ingestionService,
                                   KbIngestTaskMapper taskMapper,
                                   @Qualifier("ingestTaskSemaphore") Semaphore ingestTaskSemaphore) {
        this.ingestionService = ingestionService;
        this.taskMapper = taskMapper;
        this.ingestTaskSemaphore = ingestTaskSemaphore;
    }

    /**
     * 异步触发入库任务；队列繁忙时将任务标记为 FAILED。
     *
     * @param taskId 任务 ID
     */
    @Async("ingestTaskExecutor")
    public void runAsync(Long taskId) {
        if (!ingestTaskSemaphore.tryAcquire()) {
            KbIngestTask task = taskMapper.selectById(taskId);
            if (task != null) {
                task.setStatus(KbTaskStatus.FAILED);
                task.setProgress(100);
                task.setErrorMsg("入库队列繁忙，请稍后重试");
                task.setEndTime(LocalDateTime.now());
                taskMapper.updateById(task);
            }
            return;
        }
        try {
            ingestionService.ingest(taskId);
        } finally {
            ingestTaskSemaphore.release();
        }
    }
}
