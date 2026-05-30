package io.github.genkidoudou.web.monitor.clienttrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 前端监控批次分页查询条件。
 */
@Data
@Schema(description = "前端监控批次查询")
public class SysClientTrackQueryBo {

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize;

    @Schema(description = "traceId / serverTraceId 精确匹配")
    private String traceId;

    @Schema(description = "operationId 精确匹配")
    private String operationId;

    @Schema(description = "用户名模糊")
    private String userName;

    @Schema(description = "上报原因")
    private String reason;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "开始日期（含）")
    private LocalDate beginDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束日期（含）")
    private LocalDate endDate;
}
