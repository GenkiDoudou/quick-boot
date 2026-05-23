package com.su60.quickboot.common.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 客户端信息
 *
 * @author luyanan
 * @since 2026/2/12
 */
@Data
public class OauthClientVo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 客户端id
	 *
	 * @since 2026/01/21
	 */
	private String clientId;


	/**
	 * 客户端密钥
	 *
	 * @since 2026/01/21
	 */
	private String clientSecret;


	/**
	 * 权限范围
	 *
	 * @since 2026/01/21
	 */
	private String scope;


	/**
	 * 接口授权(多个用,隔开)
	 *
	 * @since 2026/01/21
	 */
	private String authorities;


	/**
	 * access_token 有效时间
	 *
	 * @since 2026/01/21
	 */
	private Integer accessTokenValidity;


	/**
	 * refresh_token 有效时间
	 *
	 * @since 2026/01/21
	 */
	private Integer refreshTokenValidity;


	/**
	 * ip白名单
	 *
	 * @since 2026/01/21
	 */
	private String whitelistIp;


	/**
	 * 校验类型[sys_verify_type]
	 *
	 * @since 2026/01/21
	 */
	private String verifyType;


	/**
	 * 客户端名称
	 *
	 * @since 2026/2/8
	 */
	private String clientName;


	/**
	 * 私钥
	 *
	 * @since 2026/2/8
	 */
	private String privateKey;


	/**
	 * 公钥
	 *
	 * @since 2026/2/8
	 */
	private String publicKey;
}
