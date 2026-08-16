package io.github.genkidoudou.monitor.internal.litetrace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 页面访问聚合视图：同一 pageVisitId 下多条 trace 的摘要。
 */
@Data
@Schema(description = "页面访问聚合")
public class PageVisitVo {
  /** 页面访问标识 */
  private String pageVisitId;
  /** 浏览器会话 ID */
  private String sessionId;
  /** 用户标识（登录名或 userId） */
  private String uin;
  /** 调用方/客户端名称 */
  private String callerName;
  /** 当前页面路径 */
  private String pagePath;
  /** 来源页面路径 */
  private String fromPage;
  /** 0=否 1=是；任一条 trace 失败则为 0 */
  private String okFlag;
  /** 该次访问关联的 trace 条数 */
  private Long traceCount;
  /** 累计耗时毫秒 */
  private Long durationMs;
  /** 访问开始时间，格式 yyyy-MM-dd HH:mm:ss */
  private String startedAt;
  /** 访问结束时间，格式 yyyy-MM-dd HH:mm:ss */
  private String endedAt;
}
