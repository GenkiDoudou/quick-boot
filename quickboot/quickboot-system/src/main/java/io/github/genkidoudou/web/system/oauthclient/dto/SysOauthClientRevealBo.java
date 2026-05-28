package io.github.genkidoudou.web.system.oauthclient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 查看客户端密钥前校验当前登录用户密码。
 */
@Data
public class SysOauthClientRevealBo {

    /** 当前登录用户的登录密码（明文，仅用于本次校验，不落库） */
    @NotBlank(message = "请输入当前用户密码")
    private String password;
}
