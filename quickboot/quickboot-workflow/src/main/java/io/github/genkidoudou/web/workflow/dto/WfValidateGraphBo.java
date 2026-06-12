package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 校验工作流图入参（不落库）。
 */
@Data
@Schema(description = "校验工作流图入参")
public class WfValidateGraphBo {

    @NotNull(message = "图结构不能为空")
    @Schema(description = "图 DSL")
    private WorkflowGraphDto graph;
}
