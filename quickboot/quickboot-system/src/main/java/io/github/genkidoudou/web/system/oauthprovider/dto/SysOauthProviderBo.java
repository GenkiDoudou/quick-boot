package io.github.genkidoudou.web.system.oauthprovider.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 外部 IdP 配置 BO。
 */
@Data
public class SysOauthProviderBo {

    @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
    private String providerCode;

    @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
    private String providerName;

    @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
    private String clientId;

    @NotBlank(groups = AddGroup.class)
    private String clientSecret;

    @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
    private String authorizeUrl;

    @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
    private String tokenUrl;

    private String userinfoUrl;

    private String discoveryUrl;

    @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
    private String redirectUri;

    private String enabled = "0";

    private String autoRegister = "0";

    private String remark;
}
