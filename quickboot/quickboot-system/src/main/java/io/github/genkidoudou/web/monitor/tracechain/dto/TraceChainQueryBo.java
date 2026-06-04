package io.github.genkidoudou.web.monitor.tracechain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 全链路图查询：至少提供 operationId、traceId、batchId、pageVisitId 之一，
 * 或 browserVisitId / sessionId / userName（可配合日期）用于会话级探索。
 */
@Data
@Schema(description = "全链路监控查询")
public class TraceChainQueryBo {

    @Schema(description = "前端一次用户操作 ID（首选）")
    private String operationId;

    @Schema(description = "单次 HTTP 请求 traceId")
    private String traceId;

    @Schema(description = "监控批次 batchId")
    private Long batchId;

    @Schema(description = "页面访问 pageVisitId")
    private String pageVisitId;

    @Schema(description = "浏览器访问 ID")
    private String browserVisitId;

    @Schema(description = "登录会话 sessionId")
    private String sessionId;

    @Schema(description = "用户名（模糊）")
    private String userName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "开始日期")
    private LocalDate beginDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束日期")
    private LocalDate endDate;
}
