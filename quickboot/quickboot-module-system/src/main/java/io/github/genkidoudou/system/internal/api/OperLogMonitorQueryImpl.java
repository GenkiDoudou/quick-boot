package io.github.genkidoudou.system.internal.api;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.system.api.OperLogMonitorQuery;
import io.github.genkidoudou.system.api.OperLogMonitorView;
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
}
