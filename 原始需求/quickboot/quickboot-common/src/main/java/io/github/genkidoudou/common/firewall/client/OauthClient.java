package io.github.genkidoudou.common.firewall.client;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * OAuth客户端实体
 *
 * @author luyanan
 * @since 2026-03-04
 */
@Data
public class OauthClient implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * SM2公钥（用于加密）
     */
    private String publicKey;

    /**
     * SM2私钥（服务端保存客户端公钥，此字段可选）
     */
    private String privateKey;

    /**
     * 签名密钥
     */
    private String signKey;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 备注
     */
    private String remark;
}
