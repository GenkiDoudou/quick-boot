package io.github.genkidoudou.system.api;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 操作日志只读查询（跨模块消费入口；不暴露持久化实体）。
 * <p>
 * 供监控全链路按 {@code clientOperationId} / {@code traceId} 关联操作日志。
 */
public interface OperLogMonitorQuery {

  /** 单次查询默认上限（对齐 bak 全链路）。 */
  int DEFAULT_LIMIT = 200;

  /**
   * 按前端操作 ID 集合与可选时间窗查询，结果按操作时间升序，最多 {@link #DEFAULT_LIMIT} 条。
   *
   * @param clientOperationIds 前端 operationId 集合；空则返回空列表
   * @param beginTime          起始操作时间（含），可为 {@code null}
   * @param endTime            截止操作时间（含），可为 {@code null}
   * @return 视图列表
   */
  List<OperLogMonitorView> listByClientOperationIds(
    Collection<String> clientOperationIds,
    LocalDateTime beginTime,
    LocalDateTime endTime);

  /**
   * 按 traceId 集合查询（无 operationId 时的兜底），结果按操作时间升序，最多 {@link #DEFAULT_LIMIT} 条。
   *
   * @param traceIds 请求 traceId 集合；空则返回空列表
   * @return 视图列表
   */
  List<OperLogMonitorView> listByTraceIds(Collection<String> traceIds);

  /**
   * 按单个 traceId 取最早一条（用于反查 {@code clientOperationId}）。
   *
   * @param traceId 请求 traceId
   * @return 视图；不存在时 {@code null}
   */
  OperLogMonitorView findFirstByTraceId(String traceId);

  /**
   * 时间窗半开区间 {@code [start, end)} 内的请求/错误汇总。
   *
   * @param start 起始（含）
   * @param end   截止（不含）
   * @return 汇总
   */
  OperLogSummaryView summarize(LocalDateTime start, LocalDateTime end);

  /**
   * 请求/错误分桶趋势。
   *
   * @param start  起始（含）
   * @param end    截止（不含）
   * @param hourly {@code true} 按小时，否则按日
   * @return 分桶列表
   */
  List<OperLogBucketView> trend(LocalDateTime start, LocalDateTime end, boolean hourly);

  /**
   * 日志中心：按时间窗与可选条件取最近若干条（倒序），最多 {@code limit} 条。
   */
  List<OperLogHubView> listForHub(
    LocalDateTime beginTime,
    LocalDateTime endTime,
    String operName,
    String keyword,
    Integer status,
    String traceId,
    String clientId,
    int limit);
}
