package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 运行步骤 Trace 出参。
 */
@Data
@Schema(description = "运行步骤出参")
public class WfRunStepVo {

    @Schema(description = "步骤ID")
    private Long stepId;

    @Schema(description = "节点ID")
    private String nodeId;

    @Schema(description = "节点类型")
    private String nodeType;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "入参摘要")
    private Map<String, Object> inputs;

    @Schema(description = "出参摘要")
    private Map<String, Object> outputs;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "耗时毫秒")
    private Long durationMs;

    @Schema(description = "顺序号")
    private Integer orderNo;
}
