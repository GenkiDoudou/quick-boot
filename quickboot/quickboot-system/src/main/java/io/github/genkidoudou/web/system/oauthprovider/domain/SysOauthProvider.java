package io.github.genkidoudou.web.system.oauthprovider.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 外部 IdP 配置，表 {@code sys_oauth_provider}。
 */
@Data
@TableName("sys_oauth_provider")
public class SysOauthProvider implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("provider_code")
    private String providerCode;

    private String providerName;

    private String clientId;

    private String clientSecret;

    private String authorizeUrl;

    private String tokenUrl;

    private String userinfoUrl;

    private String discoveryUrl;

    private String redirectUri;

    private String enabled;

    private String autoRegister;

    private String remark;

    @TableLogic
    private String delFlag;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
