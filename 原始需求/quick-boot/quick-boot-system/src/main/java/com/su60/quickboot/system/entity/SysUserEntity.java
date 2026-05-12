package com.su60.quickboot.system.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@Data
@Accessors(chain = true)
@TableName("sys_user")
public class SysUserEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 *
	 * @since 2024/08/07
	 */

	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;


	/**
	 * 用户账号
	 *
	 * @since 2024/08/07
	 */
	@TableField("user_name")
	private String userName;


	/**
	 * 用户昵称
	 *
	 * @since 2024/08/07
	 */
	@TableField("nick_name")
	private String nickName;


	/**
	 * 用户类型（00系统用户）
	 *
	 * @since 2024/08/07
	 */
	@TableField("user_type")
	private String userType;


	/**
	 * 用户邮箱
	 *
	 * @since 2024/08/07
	 */
	@TableField("email")
	private String email;


	/**
	 * 手机号码
	 *
	 * @since 2024/08/07
	 */
	@TableField("phonenumber")
	private String phonenumber;


	/**
	 * 用户性别（0男 1女 2未知）
	 *
	 * @since 2024/08/07
	 */
	@TableField("sex")
	private String sex;


	/**
	 * 头像地址
	 *
	 * @since 2024/08/07
	 */
	@TableField("avatar")
	private String avatar;


	/**
	 * 密码
	 *
	 * @since 2024/08/07
	 */
	@TableField("password")
	private String password;


	/**
	 * 帐号状态（0正常 1停用）
	 *
	 * @since 2024/08/07
	 */
	@TableField("status")
	private String status;


	/**
	 * 删除标志（0代表存在 2代表删除）
	 *
	 * @since 2024/08/07
	 */
	@TableLogic
	private String delFlag;


	/**
	 * 最后登录IP
	 *
	 * @since 2024/08/07
	 */
	@TableField("login_ip")
	private String loginIp;


	/**
	 * 最后登录时间
	 *
	 * @since 2024/08/07
	 */
	@TableField("login_date")
	private Date loginDate;


	/**
	 * 创建者
	 *
	 * @since 2024/08/07
	 */
	@TableField(value = "create_by", fill = FieldFill.INSERT)
	private Long createBy;


	/**
	 * 创建时间
	 *
	 * @since 2024/08/07
	 */
	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private Date createTime;


	/**
	 * 更新者
	 *
	 * @since 2024/08/07
	 */
	@TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
	private Long updateBy;


	/**
	 * 更新时间
	 *
	 * @since 2024/08/07
	 */
	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;


	/**
	 * 备注
	 *
	 * @since 2024/08/07
	 */
	@TableField("remark")
	private String remark;
	/**
	 * 部门id
	 * @since 2025/12/24
	 */

	private Long deptId;

}
