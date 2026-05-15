package io.github.genkidoudou.web.system.role.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量为用户授予角色。
 */
@Data
public class RoleGrantUsersRequest {

    @NotNull(message = "角色ID不能为空")
    @Min(1)
    private Long roleId;

    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;
}
