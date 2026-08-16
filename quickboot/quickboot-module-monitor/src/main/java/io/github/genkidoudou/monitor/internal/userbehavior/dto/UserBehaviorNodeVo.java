package io.github.genkidoudou.monitor.internal.userbehavior.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户行为时间线节点：单条 RUM 事件的展示视图。
 */
@Data
@Schema(description = "用户行为时间线节点")
public class UserBehaviorNodeVo {
  /** RUM 事件主键 */
  private Long eventId;
  /** 事件类型：pv / action / api */
  private String eventType;
  /** 页面路径 */
  private String pagePath;
  /** 来源页面路径 */
  private String fromPage;
  /** 前端动作名称 */
  private String actionName;
  /** API 请求方法 */
  private String apiMethod;
  /** API 请求 URL */
  private String apiUrl;
  /** API 耗时毫秒 */
  private Long durationMs;
  /** HTTP 状态码 */
  private String statusCode;
  /** 0=否 1=是 */
  private String okFlag;
  /** 链路标识 */
  private String traceId;
  /** 客户端操作 ID */
  private String operationId;
  /** 浏览器会话 ID */
  private String sessionId;
  /** 用户标识 */
  private String uin;
  /** 事件发生时间，格式 yyyy-MM-dd HH:mm:ss */
  private String eventTime;
}
