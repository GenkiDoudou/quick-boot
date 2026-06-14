package io.github.genkidoudou.web.ai.prompt.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提示词新增/修改入参。
 */
@Data
@Schema(description = "提示词业务入参")
public class AiPromptBo {

    @NotNull(message = "提示词 ID 不能为空", groups = UpdateGroup.class)
    @Schema(description = "提示词 ID（修改必填）")
    private Long promptId;

    @NotBlank(message = "提示词名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "名称长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "提示词名称")
    private String name;

    @Size(max = 64, message = "分类长度不能超过64", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "提示词分类")
    private String category;

    @Size(max = 500, message = "描述长度不能超过500", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "提示词描述")
    private String description;

    @Schema(description = "提示词内容")
    private String content;
}
