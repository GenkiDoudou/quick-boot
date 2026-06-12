package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发布工作流入参。
 */
@Data
@Schema(description = "发布工作流入参")
public class WfPublishBo {

    @NotNull(message = "工作流ID不能为空")
    @Schema(description = "工作流ID")
    private Long workflowId;

    @Schema(description = "版本备注")
    private String remark;
}
