package io.github.genkidoudou.web.system.oauthprovider.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 外部 IdP VO。
 */
@Data
public class SysOauthProviderVo {

    private String providerCode;

    private String providerName;

    private String clientId;

    private String authorizeUrl;

    private String tokenUrl;

    private String userinfoUrl;

    private String discoveryUrl;

    private String redirectUri;

    private String enabled;

    private String autoRegister;

    private String remark;

    private LocalDateTime createTime;
}
