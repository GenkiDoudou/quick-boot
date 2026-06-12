package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异步入库任务详情出参。
 */
@Data
@Schema(description = "异步入库任务视图对象")
public class KbIngestTaskVo {

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "任务状态：QUEUED/RUNNING/SUCCESS/FAILED")
    private String status;

    @Schema(description = "进度 0-100")
    private Integer progress;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
