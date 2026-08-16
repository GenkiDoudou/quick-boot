package io.github.genkidoudou.quartz.internal.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;

/**
 * Quartz 任务抽象：统一执行与写调度日志。
 */
@Slf4j
public abstract class AbstractQuartzJob implements Job {

    /** 统一执行入口：解析快照、执行业务、写调度日志。 */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobTaskSnapshot snapshot = JobTaskSnapshotSupport.resolve(context);
        if (snapshot == null) {
            log.warn("定时任务跳过：无法解析任务快照 jobKey={}", context.getJobDetail().getKey());
            return;
        }
        LocalDateTime start = LocalDateTime.now();
        Exception error = null;
        try {
            doExecute(context, snapshot);
        } catch (Exception e) {
            error = e;
            log.error("定时任务执行异常 jobId={}", snapshot.getJobId(), e);
            throw new JobExecutionException(e);
        } finally {
            writeLog(snapshot, start, error);
        }
    }

    protected abstract void doExecute(JobExecutionContext context, JobTaskSnapshot snapshot) throws Exception;

    private void writeLog(JobTaskSnapshot snapshot, LocalDateTime start, Exception error) {
        JobExecutionLogger logger = JobExecutionBridge.getJobExecutionLogger();
        if (logger != null) {
            logger.write(snapshot, start, error);
        }
    }
}
