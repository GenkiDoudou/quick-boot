package com.su60.quickboot.system.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
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
@TableName("sys_oauth_client")
public class SysOauthClientEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 *
	 * @since 2026/01/21
	 */

	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;


	/**
	 * 客户端id
	 *
	 * @since 2026/01/21
	 */
	@TableField("client_id")
	private String clientId;


	/**
	 * 客户端密钥
	 *
	 * @since 2026/01/21
	 */
	@TableField("client_secret")
	private String clientSecret;


	/**
	 * 权限范围
	 *
	 * @since 2026/01/21
	 */
	@TableField("scope")
	private String scope;


	/**
	 * 接口授权(多个用,隔开)
	 *
	 * @since 2026/01/21
	 */
	@TableField("authorities")
	private String authorities;


	/**
	 * access_token 有效时间
	 *
	 * @since 2026/01/21
	 */
	@TableField("access_token_validity")
	private Integer accessTokenValidity;


	/**
	 * refresh_token 有效时间
	 *
	 * @since 2026/01/21
	 */
	@TableField("refresh_token_validity")
	private Integer refreshTokenValidity;


	/**
	 * 逻辑删除
	 *
	 * @since 2026/01/21
	 */
	@TableLogic
	private String delFlag;


	/**
	 * ip白名单
	 *
	 * @since 2026/01/21
	 */
	@TableField("whitelist_ip")
	private String whitelistIp;


	/**
	 * 校验类型[sys_verify_type]
	 *
	 * @since 2026/01/21
	 */
	@TableField("verify_type")
	private String verifyType;


	/**
	 * 创建时间
	 *
	 * @since 2026/01/21
	 */
	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private Date createTime;


	/**
	 * 创建人
	 *
	 * @since 2026/01/21
	 */
	@TableField(value = "create_by", fill = FieldFill.INSERT)
	private String createBy;


	/**
	 * 修改时间
	 *
	 * @since 2026/01/21
	 */
	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;


	/**
	 * 修改人
	 *
	 * @since 2026/01/21
	 */
	@TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
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
