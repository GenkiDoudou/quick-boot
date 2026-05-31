package io.github.genkidoudou.web.monitor.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 页面跳转边：按 pageVisit 时间序相邻两页。
 */
@Data
@Schema(description = "页面跳转边")
public class ClientTrackPageFlowEdgeVo {

    @Schema(description = "来源 pageVisitId")
    private String fromPageVisitId;

    @Schema(description = "目标 pageVisitId")
    private String toPageVisitId;

    @Schema(description = "来源页面路径")
    private String fromPagePath;

    @Schema(description = "目标页面路径")
    private String toPagePath;

    @Schema(description = "来源菜单展示")
    private String fromMenuLabel;

    @Schema(description = "目标菜单展示")
    private String toMenuLabel;

    @Schema(description = "跳转时间（目标页首次出现）")
    private LocalDateTime atTime;
}
