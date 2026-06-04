package io.github.genkidoudou.web.system.exporttask.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.exporttask.QcExportProperties;
import io.github.genkidoudou.common.file.FileTemplate;
import io.github.genkidoudou.web.system.exporttask.domain.SysExportTask;
import io.github.genkidoudou.web.system.exporttask.dto.ExportSubmitResultVo;
import io.github.genkidoudou.web.system.exporttask.dto.ExportTaskQueryBo;
import io.github.genkidoudou.web.system.exporttask.dto.ExportTaskVo;
import io.github.genkidoudou.web.system.exporttask.executor.ExportTaskAsyncExecutor;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandler;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandlerRegistry;
import io.github.genkidoudou.web.system.exporttask.mapper.SysExportTaskMapper;
import io.github.genkidoudou.web.system.exporttask.service.ExportOrchestratorService;
import io.github.genkidoudou.web.system.exporttask.support.ExportSubmitOutcome;
import io.github.genkidoudou.web.system.exporttask.support.ExportTaskStatus;
import io.github.genkidoudou.web.system.file.domain.SysFile;
import io.github.genkidoudou.web.system.file.mapper.SysFileMapper;
import io.github.genkidoudou.web.system.importtask.support.ImportMode;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Excel 导出编排：按行数分流同步/异步，结果文件写入文件管理。
 */
@Service
public class ExportOrchestratorServiceImpl implements ExportOrchestratorService {

    private final QcExportProperties exportProps;
    private final BizExportHandlerRegistry handlerRegistry;
    private final SysExportTaskMapper taskMapper;
    private final SysFileMapper sysFileMapper;
    private final FileTemplate fileTemplate;
    private final ExportTaskAsyncExecutor asyncExecutor;

    public ExportOrchestratorServiceImpl(QcExportProperties exportProps,
                                         BizExportHandlerRegistry handlerRegistry,
                                         SysExportTaskMapper taskMapper,
                                         SysFileMapper sysFileMapper,
                                         FileTemplate fileTemplate,
                                         @Lazy ExportTaskAsyncExecutor asyncExecutor) {
        this.exportProps = exportProps;
        this.handlerRegistry = handlerRegistry;
        this.taskMapper = taskMapper;
        this.sysFileMapper = sysFileMapper;
        this.fileTemplate = fileTemplate;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public ExportSubmitOutcome submitForResponse(String bizType, Map<String, Object> queryParams, String mode,
                                                 Integer syncMaxRows) {
        if (!exportProps.isEnabled()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "导出编排未启用");
        }
        if (StrUtil.isBlank(bizType)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "bizType 不能为空");
        }
        BizExportHandler handler = handlerRegistry.require(bizType.trim());
        String queryJson = JSONUtil.toJsonStr(queryParams == null ? Map.of() : queryParams);
        long rowCount = handler.countRows(queryJson);
        int asyncCap = Math.max(1, exportProps.getAsyncMaxRows());
        if (rowCount > asyncCap) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "导出数据超过上限（" + asyncCap + " 条），请缩小筛选条件");
        }
        int effectiveMax = resolveEffectiveMaxRows(syncMaxRows);
        boolean async = ImportMode.ASYNC.equalsIgnoreCase(StrUtil.nullToDefault(mode, ""))
            || rowCount > effectiveMax;

        if (async) {
            SysExportTask task = newSysExportTask(bizType, queryJson, ImportMode.ASYNC, effectiveMax, rowCount,
                ExportTaskStatus.PENDING);
            taskMapper.insert(task);
            ExportSubmitResultVo vo = new ExportSubmitResultVo();
            vo.setTaskId(task.getTaskId());
            vo.setMode(ImportMode.ASYNC);
            vo.setTotalRows(rowCount);
            asyncExecutor.runAsync(task.getTaskId());
            return new ExportSubmitOutcome.AsyncAccepted(vo);
        }

        int maxRows = (int) Math.min(rowCount, effectiveMax);
        byte[] bytes = handler.writeExcelBytes(queryJson, Math.max(1, maxRows));
        String fileName = handler.defaultFileName();

        if (exportProps.isSyncWriteTask()) {
            SysExportTask audit = newSysExportTask(bizType, queryJson, ImportMode.SYNC, effectiveMax, rowCount,
                ExportTaskStatus.SUCCESS);
            audit.setProcessedRows(maxRows);
            audit.setFinishTime(LocalDateTime.now());
            taskMapper.insert(audit);
        }

        return new ExportSubmitOutcome.SyncStream(bytes, fileName);
    }

    private SysExportTask newSysExportTask(String bizType, String queryJson, String exportMode, int effectiveMax,
                                           long rowCount, String status) {
        SysExportTask task = new SysExportTask();
        task.setBizType(bizType.trim());
        task.setQueryJson(queryJson);
        task.setExportMode(exportMode);
        task.setSyncMaxRows(effectiveMax);
        task.setStatus(status);
        task.setTotalRows((int) rowCount);
        task.setProcessedRows(0);
        task.setCreateBy(currentUsername());
        task.setCreateTime(LocalDateTime.now());
        return task;
    }

    @Override
    public ExportTaskVo getTask(Long taskId) {
        SysExportTask task = requireTask(taskId);
        assertTaskOwner(task);
        return toVo(task);
    }

    @Override
    public PageInfo<ExportTaskVo> listTasks(ExportTaskQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<SysExportTask> w = Wrappers.<SysExportTask>lambdaQuery()
            .eq(SysExportTask::getCreateBy, currentUsername())
            .eq(StrUtil.isNotBlank(query.getBizType()), SysExportTask::getBizType, query.getBizType())
            .eq(StrUtil.isNotBlank(query.getStatus()), SysExportTask::getStatus, query.getStatus())
            .orderByDesc(SysExportTask::getCreateTime);
        Page<SysExportTask> mp = taskMapper.selectPage(new Page<>(pageNum, pageSize), w);
        Map<Long, String> fileNameById = loadOriginalNamesByFileIds(
            mp.getRecords().stream().map(SysExportTask::getResultFileId).collect(Collectors.toList()));
        List<ExportTaskVo> rows = new ArrayList<>();
        for (SysExportTask t : mp.getRecords()) {
            rows.add(toVo(t, fileNameById));
        }
        Page<ExportTaskVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public void executeAsyncTask(Long taskId) {
        SysExportTask task = requireTask(taskId);
        if (!ImportMode.ASYNC.equals(task.getExportMode())) {
            return;
        }
        try {
            task.setStatus(ExportTaskStatus.RUNNING);
            taskMapper.updateById(task);
            BizExportHandler handler = handlerRegistry.require(task.getBizType());
            int maxRows = Math.min(task.getTotalRows() == null ? 0 : task.getTotalRows(),
                exportProps.getAsyncMaxRows());
            runExportJob(task, handler, Math.max(1, maxRows));
        } catch (Exception e) {
            task.setStatus(ExportTaskStatus.FAILED);
            task.setErrorMessage(StrUtil.sub(StrUtil.blankToDefault(e.getMessage(), "异步导出失败"), 0, 2000));
            task.setFinishTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    private void runExportJob(SysExportTask task, BizExportHandler handler, int maxRows) {
        byte[] bytes = handler.writeExcelBytes(task.getQueryJson(), maxRows);
        String filename = handler.defaultFileName();
        String relativePath = fileTemplate.upload(bytes, filename, exportProps.getResultClassify());
        SysFile file = sysFileMapper.selectOne(Wrappers.<SysFile>lambdaQuery()
            .eq(SysFile::getRelativePath, relativePath).last("LIMIT 1"));
        if (file == null) {
            throw new WarningException(ErrorCodes.System.INTERNAL_ERROR, "导出文件登记缺失");
        }
        task.setResultFileId(file.getFileId());
        task.setTotalRows(maxRows);
        task.setProcessedRows(maxRows);
        task.setStatus(ExportTaskStatus.SUCCESS);
        task.setFinishTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private int resolveEffectiveMaxRows(Integer syncMaxRows) {
        int configured = exportProps.getSyncMaxRows();
        int cap = exportProps.getSyncMaxRowsCap();
        int effective = syncMaxRows != null ? syncMaxRows : configured;
        return Math.min(Math.max(1, effective), Math.max(1, cap));
    }

    private SysExportTask requireTask(Long taskId) {
        SysExportTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "导出任务不存在");
        }
        return task;
    }

    private void assertTaskOwner(SysExportTask task) {
        if (!currentUsername().equals(task.getCreateBy())) {
            throw new WarningException(ErrorCodes.Security.FORBIDDEN, "无权查看该导出任务");
        }
    }

    private static String currentUsername() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            return loginId == null ? "" : String.valueOf(loginId);
        } catch (Exception e) {
            return "";
        }
    }

    private ExportTaskVo toVo(SysExportTask task) {
        Map<Long, String> names = loadOriginalNamesByFileIds(
            task.getResultFileId() == null ? List.of() : List.of(task.getResultFileId()));
        return toVo(task, names);
    }

    private ExportTaskVo toVo(SysExportTask task, Map<Long, String> fileNameById) {
        ExportTaskVo vo = BeanUtil.copyProperties(task, ExportTaskVo.class);
        if (task.getResultFileId() != null && fileNameById != null) {
            vo.setFileName(fileNameById.get(task.getResultFileId()));
        }
        return vo;
    }

    private Map<Long, String> loadOriginalNamesByFileIds(List<Long> fileIds) {
        if (CollectionUtil.isEmpty(fileIds)) {
            return Map.of();
        }
        List<Long> distinct = fileIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinct.isEmpty()) {
            return Map.of();
        }
        List<SysFile> files = sysFileMapper.selectList(
            Wrappers.<SysFile>lambdaQuery().in(SysFile::getFileId, distinct));
        Map<Long, String> map = new HashMap<>(files.size());
        for (SysFile f : files) {
            if (f.getFileId() != null && StrUtil.isNotBlank(f.getOriginalName())) {
                map.put(f.getFileId(), f.getOriginalName());
            }
        }
        return map;
    }
}
