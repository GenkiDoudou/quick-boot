package io.github.genkidoudou.web.system.operlog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.monitor.operlog.OperLogProperties;
import io.github.genkidoudou.web.system.operlog.domain.SysOperLog;
import io.github.genkidoudou.web.system.operlog.dto.SysOperLogExcelRow;
import io.github.genkidoudou.web.system.operlog.dto.SysOperLogQueryBo;
import io.github.genkidoudou.web.system.operlog.dto.SysOperLogVo;
import io.github.genkidoudou.web.system.operlog.mapper.SysOperLogMapper;
import io.github.genkidoudou.web.system.operlog.service.SysOperLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志服务实现。
 */
@Service
public class SysOperLogServiceImpl implements SysOperLogService {

    private final SysOperLogMapper mapper;
    private final OperLogProperties operLogProperties;

    public SysOperLogServiceImpl(SysOperLogMapper mapper, OperLogProperties operLogProperties) {
        this.mapper = mapper;
        this.operLogProperties = operLogProperties;
    }

    @Override
    public PageInfo<SysOperLogVo> page(SysOperLogQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<SysOperLog> w = buildWrapper(query);
        applyOrder(w, query);
        Page<SysOperLog> mp = mapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<SysOperLogVo> rows = new ArrayList<>(mp.getRecords().size());
        for (SysOperLog row : mp.getRecords()) {
            rows.add(toVo(row));
        }
        Page<SysOperLogVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public SysOperLogVo getById(Long operId) {
        SysOperLog row = mapper.selectById(operId);
        if (row == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "日志不存在");
        }
        return toVo(row);
    }

    @Override
    public void export(SysOperLogQueryBo query, HttpServletResponse response) {
        LambdaQueryWrapper<SysOperLog> w = buildWrapper(query);
        applyOrder(w, query);
        int max = Math.max(1, operLogProperties.getExportMaxRows());
        List<SysOperLog> list = mapper.selectList(w.last("LIMIT " + (max + 1)));
        if (list.size() > max) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "导出数据超过上限（" + max + " 条），请缩小筛选条件");
        }
        List<SysOperLogExcelRow> rows = new ArrayList<>(list.size());
        for (SysOperLog row : list) {
            rows.add(toExcel(row));
        }
        ExcelUtils.exportExcel(rows, "operlog", SysOperLogExcelRow.class, response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> operIds) {
        if (operIds == null || operIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "请选择要删除的日志");
        }
        mapper.delete(Wrappers.<SysOperLog>lambdaQuery().in(SysOperLog::getOperId, operIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanAll() {
        mapper.delete(Wrappers.<SysOperLog>lambdaQuery());
    }

    private SysOperLogVo toVo(SysOperLog row) {
        SysOperLogVo vo = BeanUtil.copyProperties(row, SysOperLogVo.class);
        if (row.getStatus() != null) {
            vo.setStatus(String.valueOf(row.getStatus()));
        }
        return vo;
    }

    private SysOperLogExcelRow toExcel(SysOperLog row) {
        SysOperLogExcelRow excel = BeanUtil.copyProperties(row, SysOperLogExcelRow.class);
        if (row.getBusinessType() != null) {
            excel.setBusinessType(String.valueOf(row.getBusinessType()));
        }
        if (row.getStatus() != null) {
            excel.setStatus(String.valueOf(row.getStatus()));
        }
        return excel;
    }

    private Integer parseIntOrNull(String s) {
        if (StrUtil.isBlank(s)) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LambdaQueryWrapper<SysOperLog> buildWrapper(SysOperLogQueryBo query) {
        LocalDateTime begin = parseBeginTime(query.getBeginTime());
        LocalDateTime end = parseEndTime(query.getEndTime());
        Integer statusInt = parseStatus(query.getStatus());
        Integer bt = parseIntOrNull(query.getBusinessType());
        return Wrappers.<SysOperLog>lambdaQuery()
            .like(StrUtil.isNotBlank(query.getOperUrl()), SysOperLog::getOperUrl, query.getOperUrl())
            .like(StrUtil.isNotBlank(query.getTitle()), SysOperLog::getTitle, query.getTitle())
            .like(StrUtil.isNotBlank(query.getOperName()), SysOperLog::getOperName, query.getOperName())
            .eq(bt != null, SysOperLog::getBusinessType, bt)
            .eq(statusInt != null, SysOperLog::getStatus, statusInt)
            .eq(StrUtil.isNotBlank(query.getTraceId()), SysOperLog::getTraceId, query.getTraceId())
            .eq(StrUtil.isNotBlank(query.getClientOperationId()), SysOperLog::getClientOperationId, query.getClientOperationId())
            .ge(begin != null, SysOperLog::getOperTime, begin)
            .le(end != null, SysOperLog::getOperTime, end);
    }

    private Integer parseStatus(String status) {
        if (StrUtil.isBlank(status)) {
            return null;
        }
        try {
            return Integer.parseInt(status.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void applyOrder(LambdaQueryWrapper<SysOperLog> w, SysOperLogQueryBo query) {
        boolean asc = Boolean.TRUE.equals(query.getIsAsc());
        String col = query.getOrderByColumn();
        if (StrUtil.isNotBlank(col) && "operName".equalsIgnoreCase(col.trim())) {
            w.orderBy(true, asc, SysOperLog::getOperName);
            w.orderByDesc(SysOperLog::getOperTime);
            return;
        }
        if (StrUtil.isNotBlank(col) && "operTime".equalsIgnoreCase(col.trim())) {
            w.orderBy(true, asc, SysOperLog::getOperTime);
            return;
        }
        if (StrUtil.isNotBlank(col) && "costTime".equalsIgnoreCase(col.trim())) {
            w.orderBy(true, asc, SysOperLog::getCostTime);
            w.orderByDesc(SysOperLog::getOperTime);
            return;
        }
        w.orderByDesc(SysOperLog::getOperTime);
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
