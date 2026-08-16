package io.github.genkidoudou.system.internal.api;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.system.api.OperLogBucketView;
import io.github.genkidoudou.system.api.OperLogHubView;
import io.github.genkidoudou.system.api.OperLogMonitorQuery;
import io.github.genkidoudou.system.api.OperLogMonitorView;
import io.github.genkidoudou.system.api.OperLogSummaryView;
import io.github.genkidoudou.system.internal.entity.SysOperLog;
import io.github.genkidoudou.system.internal.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * {@link OperLogMonitorQuery} 实现：经 Mapper 查询后映射为 api 视图，不对外暴露实体。
 */
@Service
@RequiredArgsConstructor
public class OperLogMonitorQueryImpl implements OperLogMonitorQuery {

  private final SysOperLogMapper operLogMapper;

  @Override
  public List<OperLogMonitorView> listByClientOperationIds(
    Collection<String> clientOperationIds,
    LocalDateTime beginTime,
    LocalDateTime endTime) {
    List<String> ids = normalizeIds(clientOperationIds);
    if (ids.isEmpty()) {
      return List.of();
    }
    LambdaQueryWrapper<SysOperLog> w = Wrappers.lambdaQuery();
    w.in(SysOperLog::getClientOperationId, ids);
    if (beginTime != null) {
      w.ge(SysOperLog::getOperTime, beginTime);
    }
    if (endTime != null) {
      w.le(SysOperLog::getOperTime, endTime);
    }
    w.orderByAsc(SysOperLog::getOperTime);
    w.last("LIMIT " + DEFAULT_LIMIT);
    return operLogMapper.selectList(w).stream().map(OperLogMonitorQueryImpl::toView).toList();
  }

  @Override
  public List<OperLogMonitorView> listByTraceIds(Collection<String> traceIds) {
    List<String> ids = normalizeIds(traceIds);
    if (ids.isEmpty()) {
      return List.of();
    }
    LambdaQueryWrapper<SysOperLog> w = Wrappers.lambdaQuery();
    w.in(SysOperLog::getTraceId, ids);
    w.orderByAsc(SysOperLog::getOperTime);
    w.last("LIMIT " + DEFAULT_LIMIT);
    return operLogMapper.selectList(w).stream().map(OperLogMonitorQueryImpl::toView).toList();
  }

  @Override
  public OperLogMonitorView findFirstByTraceId(String traceId) {
    if (StrUtil.isBlank(traceId)) {
      return null;
    }
    SysOperLog log = operLogMapper.selectOne(Wrappers.<SysOperLog>lambdaQuery()
      .eq(SysOperLog::getTraceId, traceId.trim())
      .orderByAsc(SysOperLog::getOperTime)
      .last("LIMIT 1"));
    return toView(log);
  }

  @Override
  public OperLogSummaryView summarize(LocalDateTime start, LocalDateTime end) {
    OperLogSummaryView row = operLogMapper.summarizeWindow(start, end);
    if (row == null) {
      return new OperLogSummaryView(0L, 0L);
    }
    return row;
  }

  @Override
  public List<OperLogBucketView> trend(LocalDateTime start, LocalDateTime end, boolean hourly) {
    List<OperLogBucketView> rows = hourly
      ? operLogMapper.trendHourly(start, end)
      : operLogMapper.trendDaily(start, end);
    return rows == null ? List.of() : rows;
  }

  @Override
  public List<OperLogHubView> listForHub(
    LocalDateTime beginTime,
    LocalDateTime endTime,
    String operName,
    String keyword,
    Integer status,
    String traceId,
    String clientId,
    int limit) {
    int lim = Math.max(1, Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, DEFAULT_LIMIT));
    LambdaQueryWrapper<SysOperLog> w = Wrappers.lambdaQuery();
    if (beginTime != null) {
      w.ge(SysOperLog::getOperTime, beginTime);
    }
    if (endTime != null) {
      w.lt(SysOperLog::getOperTime, endTime);
    }
    if (StrUtil.isNotBlank(operName)) {
      w.like(SysOperLog::getOperName, operName.trim());
    }
    if (StrUtil.isNotBlank(keyword)) {
      String k = keyword.trim();
      w.and(n -> n.like(SysOperLog::getTitle, k).or().like(SysOperLog::getOperUrl, k));
    }
    if (status != null) {
      w.eq(SysOperLog::getStatus, status);
    }
    if (StrUtil.isNotBlank(traceId)) {
      w.eq(SysOperLog::getTraceId, traceId.trim());
    }
    if (StrUtil.isNotBlank(clientId)) {
      w.eq(SysOperLog::getClientId, clientId.trim());
    }
    w.orderByDesc(SysOperLog::getOperTime);
    w.last("LIMIT " + lim);
    return operLogMapper.selectList(w).stream().map(OperLogMonitorQueryImpl::toHubView).toList();
  }

  private static List<String> normalizeIds(Collection<String> raw) {
    if (raw == null || raw.isEmpty()) {
      return List.of();
    }
    return raw.stream()
      .filter(StrUtil::isNotBlank)
      .map(String::trim)
      .distinct()
      .toList();
  }

  private static OperLogMonitorView toView(SysOperLog log) {
    if (log == null) {
      return null;
    }
    return new OperLogMonitorView(
      log.getOperId(),
      log.getTitle(),
      log.getOperUrl(),
      log.getTraceId(),
      log.getOperTime(),
      log.getCostTime(),
      log.getStatus(),
      log.getClientOperationId());
  }

  private static OperLogHubView toHubView(SysOperLog log) {
    return new OperLogHubView(
      log.getOperId(),
      log.getTitle(),
      log.getOperName(),
      log.getOperUrl(),
      log.getTraceId(),
      log.getClientOperationId(),
      log.getClientId(),
      log.getOperTime(),
      log.getCostTime(),
      log.getStatus(),
      log.getOperIp());
  }
}
