package io.github.genkidoudou.web.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色数据权限入参。
 */
@Data
@Schema(description = "角色数据权限")
public class RoleDataScopeRequest {

    @NotNull(message = "角色ID不能为空")
    @Min(1)
    @Schema(description = "角色ID")
    private Long roleId;

    @NotBlank(message = "数据范围不能为空")
    @Schema(description = "数据范围：1~5")
    private String dataScope;

    @Schema(description = "自定义部门 id 列表（dataScope=2 时必填且非空）")
    private List<Long> deptIds;
}
