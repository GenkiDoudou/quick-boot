package io.github.genkidoudou.quartz.internal.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.quartz.internal.config.JobMonitorProperties;
import io.github.genkidoudou.quartz.internal.entity.SysJobLog;
import io.github.genkidoudou.quartz.internal.dto.SysJobLogExcelRow;
import io.github.genkidoudou.quartz.internal.dto.SysJobLogQueryBo;
import io.github.genkidoudou.quartz.internal.dto.SysJobLogVo;
import io.github.genkidoudou.quartz.internal.mapper.SysJobLogMapper;
import io.github.genkidoudou.quartz.internal.service.SysJobLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 调度日志服务实现。
 */
@Service
public class SysJobLogServiceImpl implements SysJobLogService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysJobLogMapper mapper;
    private final JobMonitorProperties properties;

    public SysJobLogServiceImpl(SysJobLogMapper mapper, JobMonitorProperties properties) {
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    public PageInfo<SysJobLogVo> page(SysJobLogQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<SysJobLog> w = buildWrapper(query);
        w.orderByDesc(SysJobLog::getCreateTime);
        Page<SysJobLog> mp = mapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<SysJobLogVo> rows = new ArrayList<>(mp.getRecords().size());
        for (SysJobLog row : mp.getRecords()) {
            rows.add(BeanUtil.copyProperties(row, SysJobLogVo.class));
        }
        Page<SysJobLogVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public SysJobLogVo getById(Long jobLogId) {
        SysJobLog row = mapper.selectById(jobLogId);
        if (row == null) {
            throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "日志不存在");
        }
        return BeanUtil.copyProperties(row, SysJobLogVo.class);
    }

    @Override
    public void addLog(SysJobLog log) {
        mapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> jobLogIds) {
        if (jobLogIds == null || jobLogIds.isEmpty()) {
            throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "请选择要删除的日志");
        }
        mapper.delete(Wrappers.<SysJobLog>lambdaQuery().in(SysJobLog::getJobLogId, jobLogIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanAll() {
        mapper.delete(Wrappers.<SysJobLog>lambdaQuery());
    }

    @Override
    public void export(SysJobLogQueryBo query, HttpServletResponse response) {
        int max = Math.max(1, properties.getExportMaxRows());
        List<SysJobLogExcelRow> rows = loadJobLogExcelRows(query, max + 1);
        if (rows.size() > max) {
            throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,
                "导出数据超过上限（" + max + " 条），请缩小筛选条件");
        }
        ExcelUtils.exportExcel(rows, "jobLog", SysJobLogExcelRow.class, response);
    }
    private List<SysJobLogExcelRow> loadJobLogExcelRows(SysJobLogQueryBo query, int maxRows) {
        LambdaQueryWrapper<SysJobLog> w = buildWrapper(query);
        w.orderByDesc(SysJobLog::getCreateTime);
        int limit = Math.max(1, maxRows);
        List<SysJobLog> list = mapper.selectList(w.last("LIMIT " + limit));
        List<SysJobLogExcelRow> rows = new ArrayList<>(list.size());
        for (SysJobLog row : list) {
            SysJobLogExcelRow excel = new SysJobLogExcelRow();
            excel.setJobLogId(row.getJobLogId());
            excel.setJobName(row.getJobName());
            excel.setJobGroup(row.getJobGroup());
            excel.setInvokeTarget(row.getInvokeTarget());
            excel.setJobMessage(row.getJobMessage());
            excel.setStatus(row.getStatus());
            if (row.getCreateTime() != null) {
                excel.setCreateTime(row.getCreateTime().format(DT_FMT));
            }
            rows.add(excel);
        }
        return rows;
    }

    private LambdaQueryWrapper<SysJobLog> buildWrapper(SysJobLogQueryBo query) {
        LocalDateTime begin = parseBeginTime(query.getBeginTime());
        LocalDateTime end = parseEndTime(query.getEndTime());
        return Wrappers.<SysJobLog>lambdaQuery()
            .like(StrUtil.isNotBlank(query.getJobName()), SysJobLog::getJobName, query.getJobName())
            .eq(StrUtil.isNotBlank(query.getJobGroup()), SysJobLog::getJobGroup, query.getJobGroup())
            .eq(StrUtil.isNotBlank(query.getStatus()), SysJobLog::getStatus, query.getStatus())
            .ge(begin != null, SysJobLog::getCreateTime, begin)
            .le(end != null, SysJobLog::getCreateTime, end);
    }

    private LocalDateTime parseBeginTime(String beginTime) {
        if (StrUtil.isBlank(beginTime)) {
            return null;
        }
        return LocalDateTime.parse(beginTime.trim() + "T00:00:00");
    }

    private LocalDateTime parseEndTime(String endTime) {
        if (StrUtil.isBlank(endTime)) {
            return null;
        }
        return LocalDateTime.of(java.time.LocalDate.parse(endTime.trim()), LocalTime.MAX);
    }
}
