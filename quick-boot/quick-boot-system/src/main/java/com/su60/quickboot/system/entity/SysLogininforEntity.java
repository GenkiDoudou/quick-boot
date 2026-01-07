package com.su60.quickboot.system.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 系统访问记录
 * </p>
 *
 * @author luyanan
 * @since 2024/11/15
 */
@Data
@Accessors(chain = true)
@TableName("sys_logininfor")
public class SysLogininforEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 访问ID
	 *
	 * @since 2024/11/15
	 */

	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;


	/**
	 * 用户id
	 *
	 * @since 2024/11/15
	 */

	private Long userId;
	/**
	 * 用户账号
	 *
	 * @since 2024/11/15
	 */
	@TableField("user_name")
	private String userName;


	/**
	 * 登录IP地址
	 *
	 * @since 2024/11/15
	 */
	@TableField("ipaddr")
	private String ipaddr;


	/**
	 * 登录地点
	 *
	 * @since 2024/11/15
	 */
	@TableField("login_location")
	private String loginLocation;


	/**
	 * 浏览器类型
	 *
	 * @since 2024/11/15
	 */
	@TableField("browser")
	private String browser;


	/**
	 * 操作系统
	 *
	 * @since 2024/11/15
	 */
	@TableField("os")
	private String os;






	/**
	 * 访问时间
	 *
	 * @since 2024/11/15
	 */
	@TableField("login_time")
	private Date loginTime;



	/**
	 * 客户端id
	 *
	 * @since 2024/11/15
	 */

	private String clientId;
	/**
	 * 登录/注销
	 * @since 2025/12/21
	 */

	private String loginType;
	/**
	 * 结果(成功/失败)
	 * @since 2025/12/21
	 */

	private String loginResult;
}
