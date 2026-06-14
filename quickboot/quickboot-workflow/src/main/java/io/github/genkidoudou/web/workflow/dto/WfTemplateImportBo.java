package io.github.genkidoudou.web.workflow.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 从已有工作流导入模板入参。
 */
@Data
@Schema(description = "从工作流导入模板")
public class WfTemplateImportBo {

    @NotNull(message = "工作流ID不能为空", groups = AddGroup.class)
    @Min(value = 1, message = "工作流ID无效", groups = AddGroup.class)
    @Schema(description = "来源工作流 ID")
    private Long workflowId;

    @Schema(description = "模板元数据（名称、编码等）")
    private WfWorkflowTemplateBo template;
}
