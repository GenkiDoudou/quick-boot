package io.github.genkidoudou.web.workflow.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 工作流模板新增/修改入参。
 */
@Data
@Schema(description = "工作流模板入参")
public class WfWorkflowTemplateBo {

    @NotNull(message = "模板ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "模板ID")
    private Long templateId;

    @NotBlank(message = "模板编码不能为空", groups = AddGroup.class)
    @Size(max = 64, message = "模板编码长度不能超过64")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*$", message = "模板编码仅允许小写字母、数字与连字符，且以字母或数字开头", groups = AddGroup.class)
    @Schema(description = "唯一编码")
    private String code;

    @NotBlank(message = "模板名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 128, message = "模板名称长度不能超过128")
    @Schema(description = "展示名称")
    private String name;

    @Size(max = 512, message = "描述长度不能超过512")
    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态：ENABLED / DISABLED")
    private String status;

    @Schema(description = "排序（升序）")
    private Integer sortOrder;

    @Schema(description = "是否内置：0否 1是（仅 admin 角色可在新增时设为内置）")
    private Integer builtin;

    @Schema(description = "图 DSL；留空则使用最小 start/end 图")
    private WorkflowGraphDto graph;
}
