package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.monitor.operlog.OperLogProperties;
import io.github.genkidoudou.common.mybatisplus.BaseServiceImpl;
import io.github.genkidoudou.system.internal.entity.SysOperLog;
import io.github.genkidoudou.system.internal.mapper.SysOperLogMapper;
import io.github.genkidoudou.system.internal.service.ISysOperLogService;
import io.github.genkidoudou.system.internal.vo.SysOperLogVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 操作日志管理服务实现。
 */
@Service
public class SysOperLogServiceImpl extends BaseServiceImpl<SysOperLogMapper, SysOperLog>
  implements ISysOperLogService {

  private final OperLogProperties operLogProperties;

  /**
   * @param operLogProperties 导出上限等配置
   */
  public SysOperLogServiceImpl(OperLogProperties operLogProperties) {
    this.operLogProperties = operLogProperties;
  }

  @Override
  public PageInfo<SysOperLogVo> page(PageRequest<SysOperLogVo> pageRequest) {
    SysOperLogVo param = pageRequest != null ? pageRequest.getParam() : null;
    return this.page(pageRequest, q -> {
      applyQuery(q, param);
      q.orderByDesc(SysOperLog::getOperTime);
    }, SysOperLogVo.class);
  }

  @Override
  public SysOperLogVo getDetail(Long operId) {
    SysOperLog row = this.getById(operId);
    if (row == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "日志不存在");
    }
    return toVo(row, SysOperLogVo.class);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void remove(Collection<Long> ids) {
    if (CollectionUtil.isEmpty(ids)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "请选择要删除的日志");
    }
    this.remove(Wrappers.<SysOperLog>lambdaQuery().in(SysOperLog::getOperId, ids));
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cleanAll() {
    this.remove(Wrappers.<SysOperLog>lambdaQuery());
  }

  @Override
  public List<SysOperLogVo> export(SysOperLogVo query) {
    SysOperLogVo q = query == null ? new SysOperLogVo() : query;
    List<SysOperLog> list = listForExport(q);
    int max = Math.max(1, operLogProperties.getExportMaxRows());
    if (list.size() > max) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,
        "导出数据超过上限（" + max + " 条），请缩小筛选条件");
    }
    return list.stream().map(x -> toVo(x, SysOperLogVo.class)).collect(Collectors.toList());
  }

  private List<SysOperLog> listForExport(SysOperLogVo query) {
    List<Long> ids = query.getIds() == null ? Collections.emptyList() : query.getIds().stream()
      .filter(Objects::nonNull).distinct().collect(Collectors.toList());
    if (!ids.isEmpty()) {
      return this.listByIds(ids);
    }
    LambdaQueryWrapper<SysOperLog> w = Wrappers.lambdaQuery();
    applyQuery(w, query);
    w.orderByDesc(SysOperLog::getOperTime);
    return this.list(w);
  }

  private void applyQuery(LambdaQueryWrapper<SysOperLog> q, SysOperLogVo param) {
    if (param == null) {
      return;
    }
    if (StrUtil.isNotBlank(param.getOperUrl())) {
      q.like(SysOperLog::getOperUrl, param.getOperUrl().trim());
    }
    if (StrUtil.isNotBlank(param.getTitle())) {
      q.like(SysOperLog::getTitle, param.getTitle().trim());
    }
    if (StrUtil.isNotBlank(param.getOperName())) {
      q.like(SysOperLog::getOperName, param.getOperName().trim());
    }
    if (param.getBusinessType() != null) {
      q.eq(SysOperLog::getBusinessType, param.getBusinessType());
    }
    if (param.getStatus() != null) {
      q.eq(SysOperLog::getStatus, param.getStatus());
    }
    if (StrUtil.isNotBlank(param.getTraceId())) {
      q.eq(SysOperLog::getTraceId, param.getTraceId().trim());
    }
    if (StrUtil.isNotBlank(param.getClientOperationId())) {
      q.eq(SysOperLog::getClientOperationId, param.getClientOperationId().trim());
    }
    if (StrUtil.isNotBlank(param.getClientId())) {
      q.eq(SysOperLog::getClientId, param.getClientId().trim());
    }
    if (param.getCostTimeMin() != null) {
      q.ge(SysOperLog::getCostTime, param.getCostTimeMin());
    }
    if (param.getCostTimeMax() != null) {
      q.le(SysOperLog::getCostTime, param.getCostTimeMax());
    }
    LocalDateTime begin = parseBeginTime(param.getBeginTime());
    LocalDateTime end = parseEndTime(param.getEndTime());
    if (begin != null) {
      q.ge(SysOperLog::getOperTime, begin);
    }
    if (end != null) {
      q.le(SysOperLog::getOperTime, end);
    }
  }

  private LocalDateTime parseBeginTime(String beginTime) {
    if (StrUtil.isBlank(beginTime)) {
      return null;
    }
    return LocalDate.parse(beginTime.trim()).atStartOfDay();
  }

  private LocalDateTime parseEndTime(String endTime) {
    if (StrUtil.isBlank(endTime)) {
      return null;
    }
    return LocalDateTime.of(LocalDate.parse(endTime.trim()), LocalTime.MAX);
  }
}
