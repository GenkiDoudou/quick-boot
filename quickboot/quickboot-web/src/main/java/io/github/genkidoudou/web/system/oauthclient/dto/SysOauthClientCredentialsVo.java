package io.github.genkidoudou.web.system.oauthclient.dto;

import lombok.Data;

/**
 * OAuth 客户端凭证（明文 secret，仅密码校验通过后返回）。
 */
@Data
public class SysOauthClientCredentialsVo {

    private String clientId;

    private String clientSecret;
}
