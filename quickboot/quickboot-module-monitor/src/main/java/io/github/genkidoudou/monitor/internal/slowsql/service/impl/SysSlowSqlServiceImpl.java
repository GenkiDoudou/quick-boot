package io.github.genkidoudou.monitor.internal.slowsql.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.monitor.slowsql.SlowSqlProperties;
import io.github.genkidoudou.monitor.internal.slowsql.dto.SysSlowSqlExcelRow;
import io.github.genkidoudou.monitor.internal.slowsql.dto.SysSlowSqlQueryBo;
import io.github.genkidoudou.monitor.internal.slowsql.dto.SysSlowSqlVo;
import io.github.genkidoudou.monitor.internal.slowsql.entity.SysSlowSql;
import io.github.genkidoudou.monitor.internal.slowsql.mapper.SysSlowSqlMapper;
import io.github.genkidoudou.monitor.internal.slowsql.service.SysSlowSqlService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 慢 SQL 服务实现。
 */
@Service
public class SysSlowSqlServiceImpl implements SysSlowSqlService {

  private final SysSlowSqlMapper mapper;
  private final SlowSqlProperties slowSqlProperties;

  public SysSlowSqlServiceImpl(SysSlowSqlMapper mapper, SlowSqlProperties slowSqlProperties) {
    this.mapper = mapper;
    this.slowSqlProperties = slowSqlProperties;
  }

  @Override
  public PageInfo<SysSlowSqlVo> page(SysSlowSqlQueryBo query) {
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
    LambdaQueryWrapper<SysSlowSql> w = buildWrapper(query);
    applyOrder(w, query);
    Page<SysSlowSql> mp = mapper.selectPage(new Page<>(pageNum, pageSize), w);
    List<SysSlowSqlVo> rows = new ArrayList<>(mp.getRecords().size());
    for (SysSlowSql row : mp.getRecords()) {
      rows.add(BeanUtil.copyProperties(row, SysSlowSqlVo.class));
    }
    Page<SysSlowSqlVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
    voPage.setRecords(rows);
    return PageInfo.from(voPage);
  }

  @Override
  public SysSlowSqlVo getById(Long slowId) {
    SysSlowSql row = mapper.selectById(slowId);
    if (row == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "记录不存在");
    }
    return BeanUtil.copyProperties(row, SysSlowSqlVo.class);
  }

  @Override
  public void export(SysSlowSqlQueryBo query, HttpServletResponse response) {
    int max = Math.max(1, slowSqlProperties.getExportMaxRows());
    List<SysSlowSqlExcelRow> rows = loadExportRows(query, max + 1);
    if (rows.size() > max) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,
        "导出数据超过上限（" + max + " 条），请缩小筛选条件");
    }
    ExcelUtils.exportExcel(rows, "slowsql", SysSlowSqlExcelRow.class, response);
  }

  private List<SysSlowSqlExcelRow> loadExportRows(SysSlowSqlQueryBo query, int maxRows) {
    LambdaQueryWrapper<SysSlowSql> w = buildWrapper(query);
    applyOrder(w, query);
    int limit = Math.max(1, maxRows);
    List<SysSlowSql> list = mapper.selectList(w.last("LIMIT " + limit));
    List<SysSlowSqlExcelRow> rows = new ArrayList<>(list.size());
    for (SysSlowSql row : list) {
      rows.add(BeanUtil.copyProperties(row, SysSlowSqlExcelRow.class));
    }
    return rows;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void removeBatch(List<Long> slowIds) {
    if (slowIds == null || slowIds.isEmpty()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "请选择要删除的记录");
    }
    mapper.delete(Wrappers.<SysSlowSql>lambdaQuery().in(SysSlowSql::getSlowId, slowIds));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void cleanAll() {
    mapper.delete(Wrappers.<SysSlowSql>lambdaQuery());
  }

  private LambdaQueryWrapper<SysSlowSql> buildWrapper(SysSlowSqlQueryBo query) {
    LocalDateTime begin = parseBeginTime(query.getBeginTime());
    LocalDateTime end = parseEndTime(query.getEndTime());
    return Wrappers.<SysSlowSql>lambdaQuery()
      .eq(StrUtil.isNotBlank(query.getSqlSource()), SysSlowSql::getSqlSource, query.getSqlSource())
      .eq(StrUtil.isNotBlank(query.getSqlType()), SysSlowSql::getSqlType, query.getSqlType())
      .like(StrUtil.isNotBlank(query.getMapperId()), SysSlowSql::getMapperId, query.getMapperId())
      .like(StrUtil.isNotBlank(query.getSqlText()), SysSlowSql::getSqlText, query.getSqlText())
      .like(StrUtil.isNotBlank(query.getRequestUri()), SysSlowSql::getRequestUri, query.getRequestUri())
      .eq(StrUtil.isNotBlank(query.getTraceId()), SysSlowSql::getTraceId, query.getTraceId())
      .ge(query.getMinCostTime() != null, SysSlowSql::getCostTime, query.getMinCostTime())
      .ge(begin != null, SysSlowSql::getCreateTime, begin)
      .le(end != null, SysSlowSql::getCreateTime, end);
  }

  private void applyOrder(LambdaQueryWrapper<SysSlowSql> w, SysSlowSqlQueryBo query) {
    boolean asc = "asc".equalsIgnoreCase(StrUtil.blankToDefault(query.getIsAsc(), "desc"));
    String col = query.getOrderByColumn();
    if (StrUtil.isNotBlank(col) && "costTime".equalsIgnoreCase(col.trim())) {
      w.orderBy(true, asc, SysSlowSql::getCostTime);
      w.orderByDesc(SysSlowSql::getCreateTime);
      return;
    }
    w.orderByDesc(SysSlowSql::getCreateTime);
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
