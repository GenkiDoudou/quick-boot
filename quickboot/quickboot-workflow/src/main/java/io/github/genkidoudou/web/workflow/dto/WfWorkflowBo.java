package io.github.genkidoudou.web.workflow.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 工作流定义新增/修改入参。
 */
@Data
@Schema(description = "工作流定义入参")
public class WfWorkflowBo {

    @NotNull(message = "工作流ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "工作流ID")
    private Long workflowId;

    @NotBlank(message = "名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 128, message = "名称长度不能超过128")
    @Schema(description = "工作流名称")
    private String name;

    @Size(max = 512, message = "描述长度不能超过512")
    @Schema(description = "描述")
    private String description;

    @Schema(description = "可选 Chat 模型 ID")
    private Long chatModelId;

    @Schema(description = "创建时使用的内置模板编码（loop-count-test / default-rag），留空则空白画布")
    private String templateCode;
}
