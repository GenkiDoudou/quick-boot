package io.github.genkidoudou.web.system.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 保存用户角色分配请求体。
 */
@Data
@Schema(description = "用户分配角色保存")
public class UserAuthRoleRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户 id")
    private Long userId;

    @NotEmpty(message = "至少选择一个角色")
    @Schema(description = "角色 id 列表")
    private List<Long> roleIds;
}
