package io.github.genkidoudou.web.system.config.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统参数业务入参。
 */
@Data
@Schema(description = "系统参数业务入参")
public class SysConfigBo {
    @NotNull(message = "参数ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "参数ID（修改必填）")
    private Long configId;

    @NotBlank(message = "参数名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "参数名称长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "参数名称")
    private String configName;

    @NotBlank(message = "参数键名不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "参数键名长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Pattern(regexp = "^[a-z0-9]+(\\.[a-z0-9]+)*$", message = "参数键名格式不正确，仅支持小写字母、数字和点分隔", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "参数键名")
    private String configKey;

    @NotBlank(message = "参数键值不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 500, message = "参数键值长度不能超过500", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "参数键值")
    private String configValue;

    @Pattern(regexp = "^[01]$", message = "系统内置标记必须为0或1", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "系统内置标记：0=否，1=是")
    private String configType;

    @Size(max = 500, message = "备注长度不能超过500", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "备注")
    private String remark;
}
