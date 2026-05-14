package io.github.genkidoudou.web.system.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员重置用户密码请求体。
 */
@Data
@Schema(description = "重置用户密码")
public class UserResetPwdRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户 id")
    private Long userId;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度须在6到100之间")
    @Schema(description = "新密码")
    private String newPassword;
}
