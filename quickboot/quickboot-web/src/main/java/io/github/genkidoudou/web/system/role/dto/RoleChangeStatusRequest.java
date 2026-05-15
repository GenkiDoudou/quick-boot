package io.github.genkidoudou.web.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 角色状态变更入参。
 */
@Data
@Schema(description = "角色状态变更")
public class RoleChangeStatusRequest {

    @NotNull(message = "角色ID不能为空")
    @Min(1)
    @Schema(description = "角色ID")
    private Long roleId;

    @NotNull(message = "状态不能为空")
    @Pattern(regexp = "^[01]$", message = "状态必须为0或1")
    @Schema(description = "状态：0 正常 1 停用")
    private String status;
}
