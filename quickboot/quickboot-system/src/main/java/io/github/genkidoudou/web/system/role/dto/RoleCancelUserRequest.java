package io.github.genkidoudou.web.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 取消单个用户的角色。
 */
@Data
@Schema(description = "角色取消单个用户")
public class RoleCancelUserRequest {

    @NotNull(message = "角色ID不能为空")
    @Min(1)
    private Long roleId;

    @NotNull(message = "用户ID不能为空")
    @Min(1)
    private Long userId;
}
