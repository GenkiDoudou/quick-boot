package io.github.genkidoudou.web.system.oauthclient.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OAuth2 授权服务器第三方应用，表 {@code sys_oauth_client}。
 */
@Data
@TableName("sys_oauth_client")
public class SysOauthClient implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("client_id")
    private String clientId;

    private String clientSecret;

    private String clientName;

    private String redirectUris;

    private String grantTypes;

    /** OAuth2 用户授权 scope（openid/profile），与接口正则授权分离 */
    private String scopes;

    /** 允许访问的接口 path（Ant 风格，如 /system/**），每行一条 */
    private String apiPathPatterns;

    /** 是否启用 Client 验签（0 否 1 是）；为 0 时带该 client_id 的请求不校验 HMAC/接口路径 */
    private String signVerify;

    private String status;

    private String isConfidential;

    private String remark;

    @TableLogic
    private String delFlag;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
