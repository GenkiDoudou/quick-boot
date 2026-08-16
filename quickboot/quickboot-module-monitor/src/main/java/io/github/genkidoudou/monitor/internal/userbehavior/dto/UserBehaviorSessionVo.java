package io.github.genkidoudou.monitor.internal.userbehavior.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户行为会话摘要：一次浏览器会话的聚合信息。
 */
@Data
@Schema(description = "用户行为会话摘要")
public class UserBehaviorSessionVo {
  /** 浏览器会话 ID */
  private String sessionId;
  /** 用户标识 */
  private String uin;
  /** 会话内首个 PV 页面路径 */
  private String firstPage;
  /** 会话内末个 PV 页面路径 */
  private String lastPage;
  /** 会话内事件总数 */
  private Long eventCount;
  /** 会话开始时间，格式 yyyy-MM-dd HH:mm:ss */
  private String startedAt;
  /** 会话结束时间，格式 yyyy-MM-dd HH:mm:ss */
  private String endedAt;
}
