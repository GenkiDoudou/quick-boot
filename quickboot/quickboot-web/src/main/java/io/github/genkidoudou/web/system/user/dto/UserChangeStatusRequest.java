package io.github.genkidoudou.web.system.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改用户状态请求体。
 */
@Data
@Schema(description = "用户改状态")
public class UserChangeStatusRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户 id")
    private Long userId;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^[01]$", message = "状态必须为0或1")
    @Schema(description = "状态：0 正常 1 停用")
    private String status;
}
