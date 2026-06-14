package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存工作流模板图 DSL 入参。
 */
@Data
@Schema(description = "保存模板图")
public class WfSaveTemplateGraphBo {

    @NotNull(message = "模板ID不能为空")
    @Schema(description = "模板ID")
    private Long templateId;

    @NotNull(message = "图 DSL 不能为空")
    @Schema(description = "图 DSL")
    private WorkflowGraphDto graph;
}
