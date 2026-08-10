package io.github.genkidoudou.monitor.internal.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 行为轨迹单页明细查询：在 timeline 概览范围下按 pageVisitId（或 pagePath）拉取操作与事件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "前端监控行为轨迹单页明细查询")
public class ClientTrackTimelinePageQueryBo extends ClientTrackTimelineQueryBo {

    @Schema(description = "页面访问 ID（与概览节点一致；无 pageVisitId 时传空并用 pagePath）")
    private String pageVisitId;

    @Schema(description = "所属登录会话 ID（与概览节点 sessionId 一致，建议传入以缩小范围）")
    private String sessionId;

    @Schema(description = "页面路径（pageVisitId 为空时用于定位未知 pageVisitId 的页面）")
    private String pagePath;
}
