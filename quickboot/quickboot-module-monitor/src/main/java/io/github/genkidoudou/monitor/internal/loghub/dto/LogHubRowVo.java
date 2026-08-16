package io.github.genkidoudou.monitor.internal.loghub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 日志中心统一行：不同来源映射为相同结构便于前端展示。
 */
@Data
@Schema(description = "日志中心统一行")
public class LogHubRowVo {
  /** 来源：page / api / sql / oper / login */
  private String source;
  /** 发生时间，格式 yyyy-MM-dd HH:mm:ss */
  private String occurredAt;
  /** 列表标题 */
  private String title;
  /** 操作人/用户标识 */
  private String actor;
  /** 状态：ok / fail */
  private String status;
  /** 来源侧主键或引用 ID */
  private String refId;
  /** 链路标识 */
  private String traceId;
  /** 客户端操作 ID */
  private String operationId;
  /** 附加摘要，如耗时、IP 等 */
  private String extra;

  /** 页面路径 / 接口 URL / SQL 摘要等 */
  private String pagePath;
  /** 来源页面路径 */
  private String fromPage;
  /** 浏览器会话 ID */
  private String sessionId;
  /** HTTP 方法（api 来源） */
  private String method;
  /** 请求 URL */
  private String url;
  /** MyBatis Mapper ID（sql 来源） */
  private String mapperId;
  /** SQL 文本摘要 */
  private String sqlText;
  /** 详情文本，展开面板展示 */
  private String detail;
  /** 客户端 ID（RUM appId / SQL clientId 等） */
  private String clientId;
}
