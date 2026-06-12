package io.github.genkidoudou.web.workflow.async;



import io.github.genkidoudou.web.workflow.constants.WfRunStatus;

import io.github.genkidoudou.web.workflow.domain.WfRun;

import io.github.genkidoudou.web.workflow.engine.WorkflowEngine;

import io.github.genkidoudou.web.workflow.engine.WorkflowContext;

import io.github.genkidoudou.web.workflow.mapper.WfRunMapper;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.scheduling.annotation.Async;

import org.springframework.stereotype.Component;



import java.time.LocalDateTime;

import java.util.concurrent.Semaphore;



/**

 * 工作流异步运行执行器（独立线程池 + Semaphore 限流，参考 {@code IngestTaskAsyncExecutor}）。

 */

@Component

public class WorkflowRunAsyncExecutor {



    private final WorkflowEngine workflowEngine;

    private final Semaphore workflowRunSemaphore;

    private final WfRunMapper runMapper;



    public WorkflowRunAsyncExecutor(WorkflowEngine workflowEngine,

                                    @Qualifier("workflowRunSemaphore") Semaphore workflowRunSemaphore,

                                    WfRunMapper runMapper) {

        this.workflowEngine = workflowEngine;

        this.workflowRunSemaphore = workflowRunSemaphore;

        this.runMapper = runMapper;

    }



    /**

     * 异步执行工作流 run。

     *

     * @param runId     运行 ID

     * @param graphJson 图 JSON

     * @param context   运行时上下文

     */

    @Async("workflowRunExecutor")

    public void runAsync(Long runId, String graphJson, WorkflowContext context) {

        if (!workflowRunSemaphore.tryAcquire()) {

            markRunFailed(runId, "异步执行队列已满，请稍后再试");

            return;

        }

        try {

            workflowEngine.execute(runId, graphJson, context);

        } finally {

            workflowRunSemaphore.release();

        }

    }



    private void markRunFailed(Long runId, String errorMsg) {

        WfRun run = runMapper.selectById(runId);

        if (run == null) {

            return;

        }

        run.setStatus(WfRunStatus.FAILED);

        run.setErrorMsg(errorMsg);

        run.setEndTime(LocalDateTime.now());

        runMapper.updateById(run);

    }

}

