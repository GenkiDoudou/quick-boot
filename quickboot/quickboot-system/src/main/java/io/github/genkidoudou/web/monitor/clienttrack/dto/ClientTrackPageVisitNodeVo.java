package io.github.genkidoudou.web.monitor.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 行为轨迹中的页面访问节点（同一 pageVisitId 下的访问批 + 按钮操作批）。
 */
@Data
@Schema(description = "监控页面访问节点")
public class ClientTrackPageVisitNodeVo {

    @Schema(description = "pageVisitId")
    private String pageVisitId;

    @Schema(description = "页面路径")
    private String pagePath;

    @Schema(description = "菜单名")
    private String menuName;

    @Schema(description = "菜单面包屑")
    private String menuBreadcrumb;

    @Schema(description = "该页首次批次时间")
    private LocalDateTime firstTime;

    @Schema(description = "页面访问批（route_enter + 初始化 API）")
    private ClientTrackActionNodeVo pageVisitBatch;

    @Schema(description = "页面上的按钮操作批")
    private List<ClientTrackActionNodeVo> actions = new ArrayList<>();
}
