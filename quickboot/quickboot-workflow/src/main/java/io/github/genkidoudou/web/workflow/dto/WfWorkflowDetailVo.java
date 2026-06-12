package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流详情出参（含草稿 graph）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工作流详情出参")
public class WfWorkflowDetailVo extends WfWorkflowVo {

    @Schema(description = "草稿版本ID")
    private Long draftVersionId;

    @Schema(description = "草稿图 DSL")
    private WorkflowGraphDto draftGraph;
}
