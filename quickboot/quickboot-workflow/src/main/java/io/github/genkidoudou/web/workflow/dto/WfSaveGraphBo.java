package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存工作流图 DSL 入参。
 */
@Data
@Schema(description = "保存工作流图入参")
public class WfSaveGraphBo {

    @NotNull(message = "工作流ID不能为空")
    @Schema(description = "工作流ID")
    private Long workflowId;

    @NotNull(message = "图结构不能为空")
    @Schema(description = "图 DSL")
    private WorkflowGraphDto graph;
}
