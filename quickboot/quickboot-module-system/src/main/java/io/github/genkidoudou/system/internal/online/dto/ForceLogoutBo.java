package io.github.genkidoudou.system.internal.online.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 强退在线会话请求。
 */
@Data
public class ForceLogoutBo {

    /** 待强退的会话 token。 */
    @NotBlank(message = "tokenId 不能为空")
    private String tokenId;
}
