package io.github.genkidoudou.system.api;

import java.time.LocalDateTime;

/**
 * 日志中心用登录日志行。
 */
public record LoginInfoHubView(
  Long infoId,
  String userName,
  String status,
  String msg,
  String ipaddr,
  String clientId,
  LocalDateTime loginTime
) {
}
