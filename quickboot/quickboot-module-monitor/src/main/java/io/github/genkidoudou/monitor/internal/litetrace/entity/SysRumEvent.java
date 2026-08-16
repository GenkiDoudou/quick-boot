package io.github.genkidoudou.monitor.internal.litetrace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 前端 RUM 原始事件，表 {@code sys_rum_event}。
 */
@Data
@TableName("sys_rum_event")
public class SysRumEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 事件主键 */
    @TableId(value = "event_id", type = IdType.ASSIGN_ID)
    private Long eventId;

    /** 应用标识 */
    private String appId;

    /** 事件类型：pv / action / api / error 等 */
    private String eventType;

    /** 链路标识 */
    private String traceId;

    /** 客户端操作 ID */
    private String operationId;

    /** 页面路径 */
    private String pagePath;

    /** 来源页面路径 */
    private String fromPage;

    /** 浏览器会话 ID */
    private String sessionId;

    /** 用户标识 */
    private String uin;

    /** 原始事件 JSON 载荷 */
    private String payloadJson;

    /** 客户端 IP */
    private String clientIp;

    /** User-Agent */
    private String ua;

    /** 客户端上报的事件时刻 */
    private LocalDateTime eventTime;

    /** 服务端入库时刻 */
    private LocalDateTime createTime;
}
