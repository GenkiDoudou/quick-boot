package io.github.genkidoudou.web.system.dict.type.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典类型业务入参。
 */
@Data
@Schema(description = "字典类型业务入参")
public class SysDictTypeBo {
    @NotNull(message = "字典ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "字典ID（修改必填）")
    private Long dictId;

    @NotBlank(message = "字典名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "字典名称长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "字典名称")
    private String dictName;

    @NotBlank(message = "字典类型不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "字典类型长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "字典类型")
    private String dictType;

    @Pattern(regexp = "^[01]$", message = "状态必须为0或1", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "状态")
    private String status;

    @Size(max = 500, message = "备注长度不能超过500", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "备注")
    private String remark;
}

