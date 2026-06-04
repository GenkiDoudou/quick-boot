package io.github.genkidoudou.web.monitor.tracechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 全链路摘要，用于页头展示与筛选回显。
 */
@Data
@Schema(description = "全链路摘要")
public class TraceChainSummaryVo {

    private String userName;
    private String pagePath;
    private String menuBreadcrumb;
    private String triggerAction;
    private String operationId;
    private String sessionId;
    private String browserVisitId;
    /** ok / warn / error */
    private String status;
    private Integer apiCount;
    private Integer behaviorEventCount;
    private Integer pageJumpCount;
}
