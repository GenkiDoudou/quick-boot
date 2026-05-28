package io.github.genkidoudou.web.system.oauthclient.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * OAuth 客户端列表/详情 VO（不含 client_secret；密钥见 {@link SysOauthClientCredentialsVo}）。
 */
@Data
public class SysOauthClientVo {

    private String clientId;

    private String clientName;

    private String redirectUris;

    private String grantTypes;

    private String scopes;

    private String apiPathPatterns;

    private String signVerify;

    private String status;

    private String isConfidential;

    private String remark;

    private LocalDateTime createTime;
}
