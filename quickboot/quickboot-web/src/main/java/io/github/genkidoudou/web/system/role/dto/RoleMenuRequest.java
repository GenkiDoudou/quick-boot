package io.github.genkidoudou.web.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色菜单权限保存入参。
 */
@Data
@Schema(description = "角色菜单保存")
public class RoleMenuRequest {

    @NotNull(message = "角色ID不能为空")
    @Min(1)
    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "菜单 id 列表（可空表示清空）")
    private List<Long> menuIds;
}
