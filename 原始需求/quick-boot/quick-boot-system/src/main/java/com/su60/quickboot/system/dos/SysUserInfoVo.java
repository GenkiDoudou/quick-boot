package com.su60.quickboot.system.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 系统用户vo类
 *
 * @author luyanan
 * @since 2024/8/7
 */
@Data
@Accessors(chain = true)
public class SysUserInfoVo {


	/**
	 * 系统用户
	 *
	 * @since 2024/8/7
	 */

	private SysUserDo user;


	/**
	 * 角色集合
	 *
	 * @since 2024/8/7
	 */

	private List<String> roles;

	/**
	 * 权限
	 *
	 * @since 2024/8/7
	 */

	private List<String> permissions;
}
