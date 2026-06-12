package io.github.genkidoudou.web.knowledge.ingest;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 入库任务调度：在事务提交后再投递异步执行，避免 {@code @Async} 线程读不到未提交的文档/任务记录。
 */
@Component
public class IngestTaskDispatcher {

    private final IngestTaskAsyncExecutor asyncExecutor;

    public IngestTaskDispatcher(IngestTaskAsyncExecutor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    /**
     * 当前存在 Spring 事务时，在 {@code afterCommit} 回调中触发；否则立即异步执行。
     *
     * @param taskId 入库任务 ID
     */
    public void dispatchAfterCommit(Long taskId) {
        if (taskId == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    asyncExecutor.runAsync(taskId);
                }
            });
            return;
        }
        asyncExecutor.runAsync(taskId);
    }

    /**
     * 立即投递异步任务（用于启动恢复等无外层事务场景）。
     *
     * @param taskId 入库任务 ID
     */
    public void dispatchNow(Long taskId) {
        if (taskId != null) {
            asyncExecutor.runAsync(taskId);
        }
    }
}
