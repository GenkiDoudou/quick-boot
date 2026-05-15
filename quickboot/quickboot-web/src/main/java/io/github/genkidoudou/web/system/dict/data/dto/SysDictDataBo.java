package io.github.genkidoudou.web.system.dict.data.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典项业务入参。
 */
@Data
@Schema(description = "字典项业务入参")
public class SysDictDataBo {
    @NotNull(message = "字典编码不能为空", groups = UpdateGroup.class)
    @Schema(description = "字典编码（修改必填）")
    private Long dictCode;

    @NotNull(message = "排序不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "排序")
    private Integer dictSort;

    @NotBlank(message = "数据标签不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "数据标签长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "数据标签")
    private String dictLabel;

    @NotBlank(message = "数据键值不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "数据键值长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "数据键值")
    private String dictValue;

    @NotBlank(message = "字典类型不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "字典类型长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "字典类型")
    private String dictType;

    @Size(max = 100, message = "样式长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "样式")
    private String cssClass;

    @Size(max = 100, message = "回显样式长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "回显样式")
    private String listClass;

    @Pattern(regexp = "^[01]$", message = "状态必须为0或1", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "状态")
    private String status;

    @Size(max = 500, message = "备注长度不能超过500", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "备注")
    private String remark;
}

