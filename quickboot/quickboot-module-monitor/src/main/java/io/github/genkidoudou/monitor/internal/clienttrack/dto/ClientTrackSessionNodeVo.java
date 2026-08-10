package io.github.genkidoudou.monitor.internal.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 单次登录会话内的页面访问与跳转（同一 sessionId 下的批次聚合）。
 */
@Data
@Schema(description = "监控行为轨迹会话段")
public class ClientTrackSessionNodeVo {

    @Schema(description = "会话 ID（登录后前端生成）")
    private String sessionId;

    @Schema(description = "浏览器访问 ID")
    private String browserVisitId;

    @Schema(description = "本会话内首个页面访问时间")
    private LocalDateTime firstTime;

    @Schema(description = "本会话内最后批次时间")
    private LocalDateTime lastTime;

    @Schema(description = "本会话内页面访问节点数")
    private Integer pageCount;

    @Schema(description = "本会话内按时间排序的页面节点")
    private List<ClientTrackPageVisitNodeVo> pages = new ArrayList<>();

    @Schema(description = "本会话内相邻页面跳转边（不跨 session）")
    private List<ClientTrackPageFlowEdgeVo> pageFlowEdges = new ArrayList<>();
}
