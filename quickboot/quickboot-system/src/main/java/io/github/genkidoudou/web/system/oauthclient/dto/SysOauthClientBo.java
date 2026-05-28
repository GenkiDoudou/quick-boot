package io.github.genkidoudou.web.system.oauthclient.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OAuth 客户端保存 BO。
 */
@Data
public class SysOauthClientBo {

    @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
    private String clientId;

    @NotBlank(groups = AddGroup.class)
    private String clientSecret;

    @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
    private String clientName;

    /** 授权码/隐式模式必填；仅 Client 签名或 client_credentials 等可留空 */
    private String redirectUris;

    @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
    private String grantTypes;

    private String apiPathPatterns;

    /** 是否验签：0 否 1 是 */
    private String signVerify = "1";

    private String status = "0";

    private String isConfidential = "1";

    private String remark;
}
