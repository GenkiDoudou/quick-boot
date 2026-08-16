package io.github.genkidoudou.system.api;

import java.time.LocalDateTime;

/**
 * 日志中心用操作日志行。
 */
public record OperLogHubView(
  Long operId,
  String title,
  String operName,
  String operUrl,
  String traceId,
  String clientOperationId,
  String clientId,
  LocalDateTime operTime,
  Long costTime,
  Integer status,
  String operIp
) {
}
