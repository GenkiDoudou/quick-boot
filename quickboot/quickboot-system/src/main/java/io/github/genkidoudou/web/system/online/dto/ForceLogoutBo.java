package io.github.genkidoudou.web.system.online.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 强退在线会话请求。
 */
@Data
public class ForceLogoutBo {

    @NotBlank(message = "tokenId 不能为空")
    private String tokenId;
}
