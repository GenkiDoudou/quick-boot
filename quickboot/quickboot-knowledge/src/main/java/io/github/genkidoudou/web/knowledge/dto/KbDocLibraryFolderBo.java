package io.github.genkidoudou.web.knowledge.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文档库目录新增/修改入参。
 */
@Data
@Schema(description = "文档库目录业务入参")
public class KbDocLibraryFolderBo {

    @NotNull(message = "目录ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "目录ID（修改必填）")
    private Long folderId;

    @NotNull(message = "父目录ID不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Min(value = 0, message = "父目录ID无效", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "父目录ID，0 为根")
    private Long parentId;

    @NotBlank(message = "目录名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "目录名称长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "目录名称")
    private String name;

    @Min(value = 0, message = "排序号不能为负", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "排序号")
    private Integer orderNum;
}
