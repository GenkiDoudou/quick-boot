package io.github.genkidoudou.monitor.internal.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 前端监控批次列表 VO。
 */
@Data
@Schema(description = "前端用户行为监控批次")
public class SysClientTrackVo {

    @Schema(description = "批次 ID")
    private Long batchId;

    @Schema(description = "前端 traceId / serverTraceId 过渡字段")
    private String traceId;

    @Schema(description = "前端操作 ID")
    private String operationId;

    @Schema(description = "浏览器访问 ID")
    private String browserVisitId;

    @Schema(description = "登录会话 ID")
    private String sessionId;

    @Schema(description = "页面访问 ID")
    private String pageVisitId;

    @Schema(description = "触发操作标识")
    private String triggerAction;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "上报原因")
    private String reason;

    @Schema(description = "页面路径摘要")
    private String pagePath;

    @Schema(description = "所属菜单名称（由 pagePath 关联 sys_menu）")
    private String menuName;

    @Schema(description = "所属菜单面包屑，如 系统管理 / 部门管理")
    private String menuBreadcrumb;

    @Schema(description = "客户端 IP")
    private String clientIp;

    @Schema(description = "事件 JSON")
    private String eventsJson;

    @Schema(description = "入库时间")
    private LocalDateTime createTime;
}
