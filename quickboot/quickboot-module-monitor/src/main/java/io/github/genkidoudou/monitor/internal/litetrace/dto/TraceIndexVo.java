package io.github.genkidoudou.monitor.internal.litetrace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 链路索引视图对象。
 */
@Data
@Schema(description = "链路索引 VO")
public class TraceIndexVo {
    /** 链路标识 */
    private String traceId;
    /** 应用标识 */
    private String appId;
    /** 根来源：browser / api / job */
    private String rootSource;
    /** 入口描述（页面路径或 API 形态） */
    private String entryName;
    /** 调用方/客户端名称 */
    private String callerName;
    /** 客户端操作 ID */
    private String operationId;
    /** 前端动作名称 */
    private String actionName;
    /** 页面路径 */
    private String pagePath;
    /** 来源页面路径 */
    private String fromPage;
    /** 用户标识 */
    private String uin;
    /** 浏览器会话 ID */
    private String sessionId;
    /** 页面访问 ID */
    private String pageVisitId;
    /** 0=否 1=是 */
    private String okFlag;
    /** HTTP 状态码字符串 */
    private String statusCode;
    /** 耗时毫秒 */
    private Long durationMs;
    /** 链路开始时间 */
    private LocalDateTime startedAt;
    /** 链路结束时间 */
    private LocalDateTime endedAt;
    /** 客户端 IP */
    private String clientIp;
    /** User-Agent */
    private String ua;
    /** 错误摘要 */
    private String errorSummary;
}
