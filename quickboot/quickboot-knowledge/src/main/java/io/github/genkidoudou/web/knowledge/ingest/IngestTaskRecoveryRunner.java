package io.github.genkidoudou.web.knowledge.ingest;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.web.knowledge.constants.KbDocStatus;
import io.github.genkidoudou.web.knowledge.constants.KbTaskStatus;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.domain.KbIngestTask;
import io.github.genkidoudou.web.knowledge.mapper.KbDocumentMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbIngestTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用启动时恢复未完成的入库任务：重新投递 {@code QUEUED} 与超时仍为 {@code RUNNING} 的任务。
 */
@Component
@ConditionalOnProperty(prefix = "qc.knowledge", name = "enabled", havingValue = "true")
public class IngestTaskRecoveryRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(IngestTaskRecoveryRunner.class);

    /** 超过该分钟数仍为 RUNNING 视为异常中断，启动时重新入队。 */
    private static final int STALE_RUNNING_MINUTES = 30;

    private final KbIngestTaskMapper taskMapper;
    private final KbDocumentMapper documentMapper;
    private final IngestTaskDispatcher taskDispatcher;

    public IngestTaskRecoveryRunner(KbIngestTaskMapper taskMapper,
                                    KbDocumentMapper documentMapper,
                                    IngestTaskDispatcher taskDispatcher) {
        this.taskMapper = taskMapper;
        this.documentMapper = documentMapper;
        this.taskDispatcher = taskDispatcher;
    }

    @Override
    public void run(ApplicationArguments args) {
        int queued = recoverQueuedTasks();
        int stale = recoverStaleRunningTasks();
        if (queued > 0 || stale > 0) {
            log.info("入库任务启动恢复完成：QUEUED={}，超时 RUNNING={}", queued, stale);
        }
    }

    private int recoverQueuedTasks() {
        List<KbIngestTask> tasks = taskMapper.selectList(
            Wrappers.<KbIngestTask>lambdaQuery().eq(KbIngestTask::getStatus, KbTaskStatus.QUEUED)
        );
        for (KbIngestTask task : tasks) {
            log.info("重新投递 QUEUED 入库任务 taskId={}, docId={}", task.getTaskId(), task.getDocId());
            taskDispatcher.dispatchNow(task.getTaskId());
        }
        return tasks.size();
    }

    private int recoverStaleRunningTasks() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(STALE_RUNNING_MINUTES);
        List<KbIngestTask> tasks = taskMapper.selectList(
            Wrappers.<KbIngestTask>lambdaQuery()
                .eq(KbIngestTask::getStatus, KbTaskStatus.RUNNING)
                .and(w -> w.lt(KbIngestTask::getStartTime, threshold)
                    .or()
                    .isNull(KbIngestTask::getStartTime))
        );
        for (KbIngestTask task : tasks) {
            resetTaskAndDocumentForRetry(task);
            log.warn("恢复超时 RUNNING 入库任务 taskId={}, docId={}", task.getTaskId(), task.getDocId());
            taskDispatcher.dispatchNow(task.getTaskId());
        }
        return tasks.size();
    }

    private void resetTaskAndDocumentForRetry(KbIngestTask task) {
        KbIngestTask upd = new KbIngestTask();
        upd.setTaskId(task.getTaskId());
        upd.setStatus(KbTaskStatus.QUEUED);
        upd.setProgress(0);
        upd.setStartTime(null);
        upd.setEndTime(null);
        upd.setErrorMsg(null);
        taskMapper.updateById(upd);

        if (task.getDocId() == null) {
            return;
        }
        KbDocument doc = documentMapper.selectById(task.getDocId());
        if (doc == null || !KbDocStatus.PARSING.equals(doc.getDocStatus())) {
            return;
        }
        KbDocument docUpd = new KbDocument();
        docUpd.setDocId(doc.getDocId());
        docUpd.setDocStatus(KbDocStatus.PENDING);
        docUpd.setErrorMsg(null);
        documentMapper.updateById(docUpd);
    }
}
