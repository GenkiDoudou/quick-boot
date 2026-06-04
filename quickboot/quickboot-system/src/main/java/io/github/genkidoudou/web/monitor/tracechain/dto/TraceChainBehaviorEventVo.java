package io.github.genkidoudou.web.monitor.tracechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 前端行为事件（route_enter / click / api_call 等），时间为相对会话锚点的毫秒。
 */
@Data
@Schema(description = "行为事件")
public class TraceChainBehaviorEventVo {

    private String id;
    private String type;
    private String label;
    private Long startMs;
    private Long endMs;
    /** ok / warn / error */
    private String status;
    private String traceId;
    private String operationId;
    private Boolean passive;
    private String pageVisitId;
    private Long batchId;
}
