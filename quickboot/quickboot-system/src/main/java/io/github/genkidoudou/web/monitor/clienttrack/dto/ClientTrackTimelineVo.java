package io.github.genkidoudou.web.monitor.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 行为轨迹聚合结果：页面跳转 + 页面内操作 + 事件明细。
 */
@Data
@Schema(description = "前端监控行为轨迹")
public class ClientTrackTimelineVo {

    @Schema(description = "browserVisitId（取自首条批次或查询条件）")
    private String browserVisitId;

    @Schema(description = "sessionId（取自首条批次或查询条件）")
    private String sessionId;

    @Schema(description = "用户名（取自首条批次或查询条件）")
    private String userName;

    @Schema(description = "参与聚合的批次总数")
    private Integer totalBatches;

    @Schema(description = "是否因超过上限被截断（请缩小时间范围）")
    private Boolean truncated;

    @Schema(description = "按时间排序的页面访问节点")
    private List<ClientTrackPageVisitNodeVo> pages = new ArrayList<>();

    @Schema(description = "相邻页面跳转边")
    private List<ClientTrackPageFlowEdgeVo> pageFlowEdges = new ArrayList<>();
}
