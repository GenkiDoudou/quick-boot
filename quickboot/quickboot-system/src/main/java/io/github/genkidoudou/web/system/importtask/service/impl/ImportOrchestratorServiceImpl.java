package io.github.genkidoudou.web.system.importtask.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.excel.ExcelFailureExport;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.file.FileTemplate;
import io.github.genkidoudou.common.importtask.QcImportProperties;
import io.github.genkidoudou.web.system.file.domain.SysFile;
import io.github.genkidoudou.web.system.file.dto.SysFileUploadVo;
import io.github.genkidoudou.web.system.file.mapper.SysFileMapper;
import io.github.genkidoudou.web.system.file.service.SysFileService;
import io.github.genkidoudou.web.system.importtask.domain.SysImportStagingRow;
import io.github.genkidoudou.web.system.importtask.domain.SysImportTask;
import io.github.genkidoudou.web.system.importtask.dto.ImportSubmitResultVo;
import io.github.genkidoudou.web.system.importtask.dto.ImportTaskQueryBo;
import io.github.genkidoudou.web.system.importtask.dto.ImportTaskVo;
import io.github.genkidoudou.web.system.importtask.executor.ImportTaskAsyncExecutor;
import io.github.genkidoudou.web.system.importtask.handler.BizImportHandler;
import io.github.genkidoudou.web.system.importtask.handler.BizImportHandlerRegistry;
import io.github.genkidoudou.web.system.importtask.handler.ImportHandlerContext;
import io.github.genkidoudou.web.system.importtask.mapper.SysImportStagingRowMapper;
import io.github.genkidoudou.web.system.importtask.mapper.SysImportTaskMapper;
import io.github.genkidoudou.web.system.importtask.service.ImportOrchestratorService;
import io.github.genkidoudou.web.system.importtask.support.ExcelRowCounter;
import io.github.genkidoudou.web.system.importtask.support.ImportHandlerContexts;
import io.github.genkidoudou.web.system.importtask.support.ImportMode;
import io.github.genkidoudou.web.system.importtask.support.ImportTaskStatus;
import io.github.genkidoudou.web.system.importtask.support.StagingValidateStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Excel 导入编排实现：文件管理存储、同步/异步分流、暂存两阶段。
 */
@Service
public class ImportOrchestratorServiceImpl implements ImportOrchestratorService {

    private final QcImportProperties importProps;
    private final BizImportHandlerRegistry handlerRegistry;
    private final SysFileService sysFileService;
    private final SysFileMapper sysFileMapper;
    private final FileTemplate fileTemplate;
    private final SysImportTaskMapper taskMapper;
    private final SysImportStagingRowMapper stagingMapper;
    private final ImportTaskAsyncExecutor asyncExecutor;

    public ImportOrchestratorServiceImpl(QcImportProperties importProps,
                                         BizImportHandlerRegistry handlerRegistry,
                                         SysFileService sysFileService,
                                         SysFileMapper sysFileMapper,
                                         FileTemplate fileTemplate,
                                         SysImportTaskMapper taskMapper,
                                         SysImportStagingRowMapper stagingMapper,
                                         @Lazy ImportTaskAsyncExecutor asyncExecutor) {
        this.importProps = importProps;
        this.handlerRegistry = handlerRegistry;
        this.sysFileService = sysFileService;
        this.sysFileMapper = sysFileMapper;
        this.fileTemplate = fileTemplate;
        this.taskMapper = taskMapper;
        this.stagingMapper = stagingMapper;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public ImportSubmitResultVo submit(MultipartFile file, String bizType, boolean updateSupport,
                                       String mode, Integer syncMaxRows, String contextJson) {
        if (!importProps.isEnabled()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "导入编排未启用");
        }
        if (file == null || file.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "导入文件不能为空");
        }
        if (StrUtil.isBlank(bizType)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "bizType 不能为空");
        }
        handlerRegistry.require(bizType);

        SysFileUploadVo uploaded = sysFileService.upload(file, importProps.getSourceClassify());
        int effectiveMax = resolveEffectiveMaxRows(syncMaxRows);
        int rowCount = countRowsFromFileId(uploaded.getFileId(), handlerRegistry.require(bizType).rowClass());
        boolean async = ImportMode.ASYNC.equalsIgnoreCase(StrUtil.nullToDefault(mode, ""))
            || rowCount > effectiveMax;

        String duplicateStrategy = updateSupport ? "overwrite" : "ignore";
        SysImportTask task = new SysImportTask();
        task.setBizType(bizType.trim());
        task.setSourceFileId(uploaded.getFileId());
        task.setImportMode(async ? ImportMode.ASYNC : ImportMode.SYNC);
        task.setSyncMaxRows(effectiveMax);
        task.setDuplicateStrategy(duplicateStrategy);
        if (StrUtil.isNotBlank(contextJson)) {
            task.setContextJson(contextJson.trim());
        }
        task.setStatus(async ? ImportTaskStatus.PENDING : ImportTaskStatus.RUNNING);
        task.setTotalRows(rowCount);
        task.setSuccessRows(0);
        task.setFailRows(0);
        task.setProcessedRows(0);
        task.setCreateBy(currentUsername());
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);

        ImportSubmitResultVo vo = new ImportSubmitResultVo();
        vo.setTaskId(task.getTaskId());
        vo.setMode(task.getImportMode());

        if (async) {
            asyncExecutor.runAsync(task.getTaskId());
            vo.setTotal((long) rowCount);
            vo.setSuccessCount(0L);
            vo.setFailCount(0L);
            return vo;
        }

        ProcessStats stats = processSync(task, updateSupport);
        fillResultVo(vo, task.getTaskId(), stats);
        return vo;
    }

    @Override
    public ImportTaskVo getTask(Long taskId) {
        SysImportTask task = requireTask(taskId);
        assertTaskOwner(task);
        return toVo(task);
    }

    @Override
    public PageInfo<ImportTaskVo> listTasks(ImportTaskQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<SysImportTask> w = Wrappers.<SysImportTask>lambdaQuery()
            .eq(SysImportTask::getCreateBy, currentUsername())
            .eq(StrUtil.isNotBlank(query.getBizType()), SysImportTask::getBizType, query.getBizType())
            .eq(StrUtil.isNotBlank(query.getStatus()), SysImportTask::getStatus, query.getStatus())
            .orderByDesc(SysImportTask::getCreateTime);
        Page<SysImportTask> mp = taskMapper.selectPage(new Page<>(pageNum, pageSize), w);
        Map<Long, String> fileNameById = loadOriginalNamesByFileIds(
            mp.getRecords().stream().map(SysImportTask::getSourceFileId).collect(Collectors.toList()));
        List<ImportTaskVo> rows = new ArrayList<>();
        for (SysImportTask t : mp.getRecords()) {
            rows.add(toVo(t, fileNameById));
        }
        Page<ImportTaskVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public void executeAsyncTask(Long taskId) {
        SysImportTask task = requireTask(taskId);
        if (!ImportMode.ASYNC.equals(task.getImportMode())) {
            return;
        }
        try {
            task.setStatus(ImportTaskStatus.RUNNING);
            taskMapper.updateById(task);
            loadStaging(task);
            boolean overwrite = "overwrite".equals(task.getDuplicateStrategy());
            ProcessStats stats = processStaging(task, overwrite);
            finishTask(task, stats);
        } catch (Exception e) {
            task.setStatus(ImportTaskStatus.FAILED);
            task.setErrorMessage(StrUtil.sub(StrUtil.blankToDefault(e.getMessage(), "异步导入失败"), 0, 2000));
            task.setFinishTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    private ProcessStats processSync(SysImportTask task, boolean updateSupport) {
        BizImportHandler handler = handlerRegistry.require(task.getBizType());
        ImportHandlerContext ctx = ImportHandlerContexts.fromTask(task);
        handler.beforeImport(ctx);
        ProcessStats stats = new ProcessStats();
        try (InputStream in = openSourceStream(task.getSourceFileId())) {
            ExcelUtils.importExcel(in, handler.rowClass(), false, null, (row, context) -> {
                int rowNo = context.readRowHolder().getRowIndex() + 1;
                String err = handler.processRow(row, updateSupport, ctx);
                if (StrUtil.isNotBlank(err)) {
                    stats.fail++;
                    if (stats.failures.size() < 5000) {
                        stats.failures.add(failure(rowNo, err, row));
                    }
                } else {
                    stats.success++;
                }
            }, (rows, context) -> {
            });
        } catch (Exception e) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "解析 Excel 失败：" + e.getMessage());
        }
        stats.total = stats.success + stats.fail;
        handler.afterImport(ctx);
        finishTask(task, stats);
        return stats;
    }

    private void loadStaging(SysImportTask task) {
        BizImportHandler handler = handlerRegistry.require(task.getBizType());
        int batchSize = Math.max(1, importProps.getStagingBatchSize());
        List<SysImportStagingRow> buffer = new ArrayList<>(batchSize);
        try (InputStream in = openSourceStream(task.getSourceFileId())) {
            ExcelUtils.importExcel(in, handler.rowClass(), false, null, (row, context) -> {
                int rowNo = context.readRowHolder().getRowIndex() + 1;
                SysImportStagingRow staging = new SysImportStagingRow();
                staging.setTaskId(task.getTaskId());
                staging.setRowNo(rowNo);
                staging.setRowJson(JSONUtil.toJsonStr(row));
                staging.setValidateStatus(StagingValidateStatus.PENDING);
                buffer.add(staging);
                if (buffer.size() >= batchSize) {
                    flushStaging(buffer);
                }
            }, (rows, context) -> {
            });
        } catch (Exception e) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "LOAD 阶段解析失败：" + e.getMessage());
        }
        flushStaging(buffer);
        task.setTotalRows(countStaging(task.getTaskId()));
        taskMapper.updateById(task);
    }

    private void flushStaging(List<SysImportStagingRow> buffer) {
        for (SysImportStagingRow row : buffer) {
            stagingMapper.insert(row);
        }
        buffer.clear();
    }

    private int countStaging(Long taskId) {
        Long c = stagingMapper.selectCount(Wrappers.<SysImportStagingRow>lambdaQuery()
            .eq(SysImportStagingRow::getTaskId, taskId));
        return c == null ? 0 : c.intValue();
    }

    private ProcessStats processStaging(SysImportTask task, boolean overwrite) {
        BizImportHandler handler = handlerRegistry.require(task.getBizType());
        ImportHandlerContext ctx = ImportHandlerContexts.fromTask(task);
        handler.beforeImport(ctx);
        ProcessStats stats = new ProcessStats();
        List<SysImportStagingRow> rows = stagingMapper.selectList(Wrappers.<SysImportStagingRow>lambdaQuery()
            .eq(SysImportStagingRow::getTaskId, task.getTaskId())
            .orderByAsc(SysImportStagingRow::getRowNo));
        int processed = 0;
        for (SysImportStagingRow staging : rows) {
            Object row = JSONUtil.toBean(staging.getRowJson(), handler.rowClass());
            String err = handler.processRow(row, overwrite, ctx);
            if (StrUtil.isNotBlank(err)) {
                staging.setValidateStatus(StagingValidateStatus.FAIL);
                staging.setErrorMsg(err);
                stats.fail++;
                stats.failures.add(failure(staging.getRowNo(), err, row));
            } else {
                staging.setValidateStatus(StagingValidateStatus.OK);
                stats.success++;
            }
            stagingMapper.updateById(staging);
            processed++;
            if (processed % 50 == 0) {
                task.setProcessedRows(processed);
                taskMapper.updateById(task);
            }
        }
        task.setProcessedRows(processed);
        stats.total = processed;
        handler.afterImport(ctx);
        return stats;
    }

    private void finishTask(SysImportTask task, ProcessStats stats) {
        task.setTotalRows(stats.total);
        task.setSuccessRows(stats.success);
        task.setFailRows(stats.fail);
        if (!stats.failures.isEmpty()) {
            task.setErrorFileId(uploadErrorFile(stats.failures, handlerRegistry.require(task.getBizType()).rowClass(), task.getBizType()));
        }
        task.setStatus(ImportTaskStatus.SUCCESS);
        task.setFinishTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private Long uploadErrorFile(List<ExcelFailureExport.FailureItem> failures, Class<?> rowClass, String bizType) {
        byte[] bytes = ExcelFailureExport.writeBytes(rowClass, failures);
        String filename = "import-error-" + bizType.replace(':', '-') + ".xlsx";
        SysFileUploadVo uploaded = sysFileService.uploadBytes(bytes, filename, importProps.getErrorClassify());
        return uploaded.getFileId();
    }

    @Override
    public SysFileService.DownloadPayload downloadErrorFile(Long fileId) {
        if (fileId == null || fileId < 1) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文件ID不能为空");
        }
        String user = currentUsername();
        Long owned = taskMapper.selectCount(Wrappers.<SysImportTask>lambdaQuery()
            .eq(SysImportTask::getErrorFileId, fileId)
            .eq(SysImportTask::getCreateBy, user));
        boolean ownedByUser = owned != null && owned > 0;
        if (!ownedByUser) {
            try {
                StpUtil.checkPermission("system:file:download");
            } catch (Exception e) {
                throw new WarningException(ErrorCodes.Security.FORBIDDEN, "无权下载该失败明细");
            }
        }
        return sysFileService.download(fileId);
    }

    private ExcelFailureExport.FailureItem failure(int rowNo, String msg, Object sourceRow) {
        return new ExcelFailureExport.FailureItem(rowNo, msg, sourceRow);
    }

    private int countRowsFromFileId(Long fileId, Class<?> rowClass) {
        try (InputStream in = openSourceStream(fileId)) {
            return ExcelRowCounter.countDataRows(in, rowClass);
        } catch (Exception e) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "统计行数失败：" + e.getMessage());
        }
    }

    private InputStream openSourceStream(Long fileId) throws Exception {
        SysFile file = sysFileMapper.selectById(fileId);
        if (file == null || file.getDeleted() != null && file.getDeleted() == 1) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "源文件不存在");
        }
        Resource res = fileTemplate.download(file.getRelativePath());
        return res.getInputStream();
    }

    private int resolveEffectiveMaxRows(Integer syncMaxRows) {
        int configured = importProps.getSyncMaxRows();
        int cap = importProps.getSyncMaxRowsCap();
        int effective = syncMaxRows != null ? syncMaxRows : configured;
        return Math.min(Math.max(1, effective), Math.max(1, cap));
    }

    private SysImportTask requireTask(Long taskId) {
        SysImportTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "导入任务不存在");
        }
        return task;
    }

    private void assertTaskOwner(SysImportTask task) {
        String user = currentUsername();
        if (!user.equals(task.getCreateBy())) {
            throw new WarningException(ErrorCodes.Security.FORBIDDEN, "无权查看该导入任务");
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

    private ImportTaskVo toVo(SysImportTask task) {
        Map<Long, String> names = loadOriginalNamesByFileIds(
            task.getSourceFileId() == null ? List.of() : List.of(task.getSourceFileId()));
        return toVo(task, names);
    }

    private ImportTaskVo toVo(SysImportTask task, Map<Long, String> fileNameById) {
        ImportTaskVo vo = BeanUtil.copyProperties(task, ImportTaskVo.class);
        if (task.getSourceFileId() != null && fileNameById != null) {
            vo.setFileName(fileNameById.get(task.getSourceFileId()));
        }
        return vo;
    }

    /** 批量解析 sys_file 原始文件名，供列表避免 N+1。 */
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

    private void fillResultVo(ImportSubmitResultVo vo, Long taskId, ProcessStats stats) {
        SysImportTask task = taskMapper.selectById(taskId);
        vo.setTotal((long) stats.total);
        vo.setSuccessCount((long) stats.success);
        vo.setFailCount((long) stats.fail);
        if (task != null) {
            vo.setErrorFileId(task.getErrorFileId());
        }
    }

    private static final class ProcessStats {
        int total;
        int success;
        int fail;
        final List<ExcelFailureExport.FailureItem> failures = new ArrayList<>();
    }
}
