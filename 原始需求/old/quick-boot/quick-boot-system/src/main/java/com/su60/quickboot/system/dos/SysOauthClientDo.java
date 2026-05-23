package com.su60.quickboot.system.dos;

import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * <p>
 * 客户端管理
 * </p>
 *
 * @author luyanan
 * @since 2026/01/21
 */
@Data
@Accessors(chain = true)
public class SysOauthClientDo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * ID
	 *
	 * @since 2026/01/21
	 */
	@NotNull(groups = UpdateGroup.class, message = "ID 不能为空")
	private Long id;


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
	 * 逻辑删除
	 *
	 * @since 2026/01/21
	 */
	@JsonIgnore
	private String delFlag;


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
	 * 创建时间
	 *
	 * @since 2026/01/21
	 */
	private Date createTime;


	/**
	 * 创建人
	 *
	 * @since 2026/01/21
	 */
	@JsonIgnore
	private String createBy;


	/**
	 * 修改时间
	 *
	 * @since 2026/01/21
	 */
	@JsonIgnore
	private Date updateTime;


	/**
	 * 修改人
	 *
	 * @since 2026/01/21
	 */
	@JsonIgnore
	private String updateBy;

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


	/**
	 * 状态
	 *
	 * @since 2026/2/8
	 */
	private String status;
}
