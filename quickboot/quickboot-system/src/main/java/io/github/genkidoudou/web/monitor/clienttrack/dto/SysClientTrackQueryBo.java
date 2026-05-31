package io.github.genkidoudou.web.monitor.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 前端监控批次分页查询条件。
 */
@Data
@Schema(description = "前端监控批次查询")
public class SysClientTrackQueryBo {

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize;

    @Schema(description = "批次 ID 精确匹配")
    private Long batchId;

    @Schema(description = "traceId / serverTraceId 精确匹配")
    private String traceId;

    @Schema(description = "operationId 精确匹配")
    private String operationId;

    @Schema(description = "browserVisitId 精确匹配（串联同一次浏览器访问）")
    private String browserVisitId;

    @Schema(description = "sessionId 精确匹配（串联同一会话全部批次）")
    private String sessionId;

    @Schema(description = "pageVisitId 精确匹配")
    private String pageVisitId;

    @Schema(description = "用户名模糊")
    private String userName;

    @Schema(description = "所属菜单名称模糊（由 pagePath 关联 sys_menu 解析后匹配）")
    private String menuName;

    @Schema(description = "页面路径模糊")
    private String pagePath;

    @Schema(description = "触发操作模糊")
    private String triggerAction;

    @Schema(description = "上报原因")
    private String reason;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "开始日期（含）")
    private LocalDate beginDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束日期（含）")
    private LocalDate endDate;
}
