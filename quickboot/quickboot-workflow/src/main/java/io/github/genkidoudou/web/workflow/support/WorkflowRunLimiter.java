package io.github.genkidoudou.web.workflow.support;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import io.github.genkidoudou.common.exception.ErrorCodes;

import io.github.genkidoudou.common.exception.WarningException;

import io.github.genkidoudou.web.workflow.config.WorkflowProperties;

import io.github.genkidoudou.web.workflow.constants.WfRunStatus;

import io.github.genkidoudou.web.workflow.constants.WfTriggerType;

import io.github.genkidoudou.web.workflow.domain.WfRun;

import io.github.genkidoudou.web.workflow.mapper.WfRunMapper;

import org.springframework.stereotype.Component;



import java.time.LocalDateTime;

import java.util.List;



/**

 * 用户并发运行限制器：限制单用户同时 RUNNING/QUEUED 的运行实例数。

 * <p>校验前会回收超时未结束的运行记录，避免异常中断后长期占用并发槽。</p>

 */

@Component

public class WorkflowRunLimiter {



    private final WfRunMapper runMapper;

    private final WorkflowProperties properties;



    public WorkflowRunLimiter(WfRunMapper runMapper, WorkflowProperties properties) {

        this.runMapper = runMapper;

        this.properties = properties;

    }



    /**

     * 校验用户是否可发起新运行，超限则抛出 {@link ErrorCodes.Biz#WORKFLOW_CONCURRENT_LIMIT}。

     *

     * @param userId 用户标识（createBy）

     */

    public void checkUserLimit(String userId) {

        if (userId == null || userId.isBlank()) {

            return;

        }

        reclaimStaleRuns(userId);

        long active = runMapper.selectCount(activeRunWrapper(userId));

        if (active >= properties.getMaxConcurrentRunsPerUser()) {

            throw new WarningException(ErrorCodes.Biz.WORKFLOW_CONCURRENT_LIMIT,

                "并发运行数已达上限（" + properties.getMaxConcurrentRunsPerUser() + "），请稍后再试");

        }

    }



    /**

     * 将超时仍处于 QUEUED/RUNNING 的运行标记为失败，释放并发槽。

     *

     * @param userId 用户标识

     */

    public void reclaimStaleRuns(String userId) {

        if (userId == null || userId.isBlank()) {

            return;

        }

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime debugThreshold = now.minusNanos(properties.getSyncDebugTimeoutMs() * 1_000_000L);

        LocalDateTime asyncThreshold = now.minusNanos(properties.getAsyncTimeoutMs() * 1_000_000L);

        LocalDateTime queuedThreshold = now.minusMinutes(2);



        List<WfRun> staleRuns = runMapper.selectList(

            new LambdaQueryWrapper<WfRun>()

                .eq(WfRun::getCreateBy, userId)

                .in(WfRun::getStatus, WfRunStatus.QUEUED, WfRunStatus.RUNNING)

        );



        for (WfRun run : staleRuns) {

            if (!shouldReclaim(run, debugThreshold, asyncThreshold, queuedThreshold)) {

                continue;

            }

            runMapper.update(null, new LambdaUpdateWrapper<WfRun>()

                .eq(WfRun::getRunId, run.getRunId())

                .in(WfRun::getStatus, WfRunStatus.QUEUED, WfRunStatus.RUNNING)

                .set(WfRun::getStatus, WfRunStatus.FAILED)

                .set(WfRun::getErrorMsg, "运行超时或异常中断，已自动回收")

                .set(WfRun::getEndTime, now));

        }

    }



    private boolean shouldReclaim(WfRun run, LocalDateTime debugThreshold,

                                  LocalDateTime asyncThreshold, LocalDateTime queuedThreshold) {

        LocalDateTime reference = run.getStartTime() != null ? run.getStartTime() : run.getCreateTime();

        if (reference == null) {

            return true;

        }

        if (WfRunStatus.QUEUED.equals(run.getStatus()) && run.getStartTime() == null

            && run.getCreateTime() != null && run.getCreateTime().isBefore(queuedThreshold)) {

            return true;

        }

        if (WfTriggerType.DEBUG.equals(run.getTriggerType())) {

            return reference.isBefore(debugThreshold);

        }

        return reference.isBefore(asyncThreshold);

    }



    private LambdaQueryWrapper<WfRun> activeRunWrapper(String userId) {

        return new LambdaQueryWrapper<WfRun>()

            .eq(WfRun::getCreateBy, userId)

            .in(WfRun::getStatus, WfRunStatus.QUEUED, WfRunStatus.RUNNING);

    }

}

