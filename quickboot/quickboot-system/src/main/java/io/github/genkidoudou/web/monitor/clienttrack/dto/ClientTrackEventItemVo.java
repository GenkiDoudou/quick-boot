package io.github.genkidoudou.web.monitor.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 行为轨迹中的单条事件（click / API / route 等）。
 */
@Data
@Schema(description = "监控事件项")
public class ClientTrackEventItemVo {

    @Schema(description = "事件类型：click/api_call/route_enter 等")
    private String type;

    @Schema(description = "客户端时间戳 ms")
    private Long ts;

    @Schema(description = "展示标题")
    private String label;

    @Schema(description = "API 路径")
    private String url;

    @Schema(description = "HTTP 方法")
    private String method;

    @Schema(description = "耗时 ms")
    private Long cost;

    @Schema(description = "serverTraceId")
    private String serverTraceId;

    @Schema(description = "原始事件 JSON 片段，供详情抽屉")
    private String rawJson;
}
