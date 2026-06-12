package io.github.genkidoudou.web.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.stp.StpUtil;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.workflow.async.WorkflowRunAsyncExecutor;
import io.github.genkidoudou.web.workflow.config.WorkflowProperties;
import io.github.genkidoudou.web.workflow.constants.WfRunMode;
import io.github.genkidoudou.web.workflow.constants.WfRunStatus;
import io.github.genkidoudou.web.workflow.constants.WfTriggerType;
import io.github.genkidoudou.web.workflow.domain.WfRun;
import io.github.genkidoudou.web.workflow.domain.WfRunStep;
import io.github.genkidoudou.web.workflow.domain.WfWorkflowVersion;
import io.github.genkidoudou.web.workflow.dto.WfRunAsyncBo;
import io.github.genkidoudou.web.workflow.dto.WfRunDebugBo;
import io.github.genkidoudou.web.workflow.dto.WfRunDetailVo;
import io.github.genkidoudou.web.workflow.dto.WfRunQueryBo;
import io.github.genkidoudou.web.workflow.dto.WfRunStepVo;
import io.github.genkidoudou.web.workflow.dto.WfRunVo;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.engine.WorkflowEngine;
import io.github.genkidoudou.web.workflow.mapper.WfRunMapper;
import io.github.genkidoudou.web.workflow.mapper.WfRunStepMapper;
import io.github.genkidoudou.web.workflow.service.WorkflowRunService;
import io.github.genkidoudou.web.workflow.support.WorkflowRunLimiter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流运行服务实现。
 */
@Service
public class WorkflowRunServiceImpl implements WorkflowRunService {

    private final WorkflowDefinitionServiceImpl definitionService;
    private final WorkflowEngine workflowEngine;
    private final WorkflowRunAsyncExecutor asyncExecutor;
    private final WfRunMapper runMapper;
    private final WfRunStepMapper runStepMapper;
    private final WorkflowProperties properties;
    private final WorkflowRunLimiter runLimiter;

    public WorkflowRunServiceImpl(WorkflowDefinitionServiceImpl definitionService,
                                  WorkflowEngine workflowEngine,
                                  WorkflowRunAsyncExecutor asyncExecutor,
                                  WfRunMapper runMapper,
                                  WfRunStepMapper runStepMapper,
                                  WorkflowProperties properties,
                                  WorkflowRunLimiter runLimiter) {
        this.definitionService = definitionService;
        this.workflowEngine = workflowEngine;
        this.asyncExecutor = asyncExecutor;
        this.runMapper = runMapper;
        this.runStepMapper = runStepMapper;
        this.properties = properties;
        this.runLimiter = runLimiter;
    }

    @Override
    public WfRunDetailVo debugRun(WfRunDebugBo req) {
        String userId = currentUserId();
        runLimiter.reclaimStaleRuns(userId);
        // 设计器 Debug 同步运行不计入用户并发上限，避免调试时频繁触发限制
        boolean useDraft = req.getUseDraft() == null || req.getUseDraft();
        WfWorkflowVersion version = definitionService.resolveRunVersion(req.getWorkflowId(), useDraft, !useDraft);
        String graphJson = resolveGraphJson(req.getGraph(), version.getGraphJson());
        WorkflowGraphDto graph = JSONUtil.toBean(graphJson, WorkflowGraphDto.class);
        String startNodeId = WorkflowDefinitionServiceImpl.findStartNodeId(graph);
        boolean stream = Boolean.TRUE.equals(req.getStream());

        WfRun run = createRun(req.getWorkflowId(), version.getVersionId(), WfTriggerType.DEBUG,
            WfRunMode.SYNC, userId, req.getInputs(), stream);
        WorkflowContext context = buildContext(run.getRunId(), req.getWorkflowId(), startNodeId, req.getInputs(), req.getKbId(), userId, stream);

        // 同步在同一线程执行，且本方法不加 @Transactional，避免 RR 隔离级别下 getInfo 读不到步骤 Trace
        try {
            workflowEngine.execute(run.getRunId(), graphJson, context);
        } catch (Exception ex) {
            markFailed(run.getRunId(), ex.getMessage());
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_RUN_FAILED, ex.getMessage());
        }
        return getInfo(run.getRunId());
    }

    @Override
    public WfRunVo asyncRun(WfRunAsyncBo req) {
        String userId = currentUserId();
        runLimiter.reclaimStaleRuns(userId);
        runLimiter.checkUserLimit(userId);
        boolean usePublished = req.getUsePublished() == null || req.getUsePublished();
        WfWorkflowVersion version = definitionService.resolveRunVersion(req.getWorkflowId(), !usePublished, usePublished);
        String graphJson = resolveGraphJson(req.getGraph(), version.getGraphJson());
        WorkflowGraphDto graph = JSONUtil.toBean(graphJson, WorkflowGraphDto.class);
        String startNodeId = WorkflowDefinitionServiceImpl.findStartNodeId(graph);
        boolean stream = Boolean.TRUE.equals(req.getStream());

        WfRun run = createRun(req.getWorkflowId(), version.getVersionId(), WfTriggerType.ASYNC,
            WfRunMode.ASYNC, userId, req.getInputs(), stream);
        WorkflowContext context = buildContext(run.getRunId(), req.getWorkflowId(), startNodeId, req.getInputs(), req.getKbId(), userId, stream);
        asyncExecutor.runAsync(run.getRunId(), graphJson, context);
        return BeanUtil.copyProperties(runMapper.selectById(run.getRunId()), WfRunVo.class);
    }

    @Override
    public WfRunDetailVo getInfo(Long runId) {
        WfRun run = runMapper.selectById(runId);
        if (run == null) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_NOT_FOUND, "运行记录不存在");
        }
        WfRunDetailVo vo = BeanUtil.copyProperties(run, WfRunDetailVo.class);
        vo.setStreamEnabled(run.getStreamEnabled() != null && run.getStreamEnabled() == 1);
        if (StrUtil.isNotBlank(run.getInputsJson())) {
            vo.setInputs(JSONUtil.parseObj(run.getInputsJson()));
        }
        if (StrUtil.isNotBlank(run.getOutputsJson())) {
            vo.setOutputs(JSONUtil.parseObj(run.getOutputsJson()));
        }
        List<WfRunStep> steps = runStepMapper.selectList(Wrappers.<WfRunStep>lambdaQuery()
            .eq(WfRunStep::getRunId, runId)
            .orderByAsc(WfRunStep::getOrderNo));
        List<WfRunStepVo> stepVos = new ArrayList<>();
        for (WfRunStep step : steps) {
            WfRunStepVo stepVo = BeanUtil.copyProperties(step, WfRunStepVo.class);
            if (StrUtil.isNotBlank(step.getInputsJson())) {
                stepVo.setInputs(JSONUtil.parseObj(step.getInputsJson()));
            }
            if (StrUtil.isNotBlank(step.getOutputsJson())) {
                stepVo.setOutputs(JSONUtil.parseObj(step.getOutputsJson()));
            }
            stepVos.add(stepVo);
        }
        vo.setSteps(stepVos);
        return vo;
    }

    @Override
    public PageInfo<WfRunVo> page(WfRunQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<WfRun> wrapper = Wrappers.<WfRun>lambdaQuery()
            .eq(query.getWorkflowId() != null, WfRun::getWorkflowId, query.getWorkflowId())
            .eq(StrUtil.isNotBlank(query.getStatus()), WfRun::getStatus, query.getStatus())
            .orderByDesc(WfRun::getCreateTime);
        Page<WfRun> mp = runMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<WfRunVo> rows = new ArrayList<>();
        for (WfRun row : mp.getRecords()) {
            WfRunVo vo = BeanUtil.copyProperties(row, WfRunVo.class);
            vo.setStreamEnabled(row.getStreamEnabled() != null && row.getStreamEnabled() == 1);
            rows.add(vo);
        }
        Page<WfRunVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public WorkflowContext buildContext(Long runId, Long workflowId, String startNodeId, Map<String, Object> inputs,
                                          Long kbId, String userId, boolean stream) {
        WorkflowContext context = new WorkflowContext(runId, startNodeId);
        if (inputs != null) {
            context.getRunInputs().putAll(inputs);
        }
        context.getSysVariables().put("runId", runId);
        if (workflowId != null) {
            context.getSysVariables().put("workflowId", workflowId);
        }
        if (kbId != null) {
            context.getSysVariables().put("kbId", kbId);
        }
        if (userId != null) {
            context.getSysVariables().put("userId", userId);
        }
        context.setStreamEnabled(stream);
        return context;
    }

    private WfRun createRun(Long workflowId, Long versionId, String triggerType, String runMode,
                            String userId, Map<String, Object> inputs, boolean stream) {
        WfRun run = new WfRun();
        run.setWorkflowId(workflowId);
        run.setVersionId(versionId);
        run.setTriggerType(triggerType);
        run.setRunMode(runMode);
        run.setStatus(WfRunStatus.QUEUED);
        run.setInputsJson(JSONUtil.toJsonStr(inputs == null ? Map.of() : inputs));
        run.setStreamEnabled(stream ? 1 : 0);
        run.setCreateBy(userId == null ? "" : userId);
        run.setCreateTime(LocalDateTime.now());
        runMapper.insert(run);
        return run;
    }

    private void markFailed(Long runId, String errorMsg) {
        WfRun run = runMapper.selectById(runId);
        if (run != null) {
            run.setStatus(WfRunStatus.FAILED);
            run.setErrorMsg(errorMsg);
            run.setEndTime(LocalDateTime.now());
            runMapper.updateById(run);
        }
    }

    private String currentUserId() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        return loginId == null ? "" : String.valueOf(loginId);
    }

    /**
     * 解析运行使用的 graph JSON：设计器传入当前画布时优先，否则回落 DB 草稿/发布版。
     */
    private String resolveGraphJson(WorkflowGraphDto requestGraph, String versionGraphJson) {
        if (requestGraph != null && requestGraph.getNodes() != null && !requestGraph.getNodes().isEmpty()) {
            return JSONUtil.toJsonStr(requestGraph);
        }
        return versionGraphJson;
    }
}
