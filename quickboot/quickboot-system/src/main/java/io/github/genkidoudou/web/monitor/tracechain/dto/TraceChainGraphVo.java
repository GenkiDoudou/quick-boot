package io.github.genkidoudou.web.monitor.tracechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 全链路聚合结果：对齐前端 Network 视图（页面跳转 + 行为明细 + 后端资源行）。
 */
@Data
@Schema(description = "全链路图")
public class TraceChainGraphVo {

    @Schema(description = "摘要")
    private TraceChainSummaryVo summary = new TraceChainSummaryVo();

    @Schema(description = "会话时间轴最大相对毫秒")
    private Long timelineMaxMs = 0L;

    @Schema(description = "页面跳转步骤")
    private List<TraceChainPageJumpVo> pageJumps = new ArrayList<>();

    @Schema(description = "按 pageVisitId 分组的前端行为事件")
    private List<TraceChainBehaviorPageVo> behaviorByPage = new ArrayList<>();

    @Schema(description = "后端节点（api / oper_log / slow_sql）")
    private List<TraceChainBackendNodeVo> backendNodes = new ArrayList<>();

    @Schema(description = "是否因条数限制截断")
    private Boolean truncated = false;

    @Schema(description = "提示信息")
    private List<String> warnings = new ArrayList<>();
}
