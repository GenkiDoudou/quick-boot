package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 运行实例列表出参。
 */
@Data
@Schema(description = "运行实例出参")
public class WfRunVo {

    @Schema(description = "运行ID")
    private Long runId;

    @Schema(description = "工作流ID")
    private Long workflowId;

    @Schema(description = "版本ID")
    private Long versionId;

    @Schema(description = "触发类型")
    private String triggerType;

    @Schema(description = "运行模式")
    private String runMode;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "耗时毫秒")
    private Long durationMs;

    @Schema(description = "是否流式")
    private Boolean streamEnabled;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "创建人")
    private String createBy;
}
