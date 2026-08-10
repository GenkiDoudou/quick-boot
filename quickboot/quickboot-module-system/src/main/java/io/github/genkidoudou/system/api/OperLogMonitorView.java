package io.github.genkidoudou.system.api;

import java.time.LocalDateTime;

/**
 * 跨模块操作日志只读视图（全链路聚合所需字段；不含持久化实体）。
 *
 * @param operId             操作日志主键
 * @param title              模块标题
 * @param operUrl            请求 URL
 * @param traceId            请求 traceId
 * @param operTime           操作时间
 * @param costTime           耗时毫秒
 * @param status             0 正常 / 1 异常
 * @param clientOperationId  前端一次用户操作 ID
 */
public record OperLogMonitorView(
  Long operId,
  String title,
  String operUrl,
  String traceId,
  LocalDateTime operTime,
  Long costTime,
  Integer status,
  String clientOperationId
) {
}
