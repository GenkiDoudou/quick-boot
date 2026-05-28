package io.github.genkidoudou.web.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色下用户分页查询（已分配/未分配共用）。
 */
@Data
@Schema(description = "角色分配用户查询")
public class SysRoleAuthUserQueryBo {

    @Min(1)
    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @NotNull(message = "角色ID不能为空")
    @Min(1)
    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "用户账号（模糊）")
    private String userName;

    @Schema(description = "用户昵称（模糊）")
    private String nickName;
}
