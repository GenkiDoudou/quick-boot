package io.github.genkidoudou.web.monitor.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 行为轨迹聚合查询：browserVisitId / sessionId / userName 至少填一项。
 */
@Data
@Schema(description = "前端监控行为轨迹查询")
public class ClientTrackTimelineQueryBo {

    @Schema(description = "browserVisitId 精确匹配")
    private String browserVisitId;

    @Schema(description = "sessionId 精确匹配")
    private String sessionId;

    @Schema(description = "用户名模糊（合并时间范围内全部批次）")
    private String userName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "开始日期（含）")
    private LocalDate beginDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束日期（含）")
    private LocalDate endDate;
}
