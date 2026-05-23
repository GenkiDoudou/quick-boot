package com.su60.quickboot.core.security;

import com.su60.quickboot.data.datascope.DataScopeType;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 登录用户
 *
 * @author luyanan
 * @since 2024/06/30
 **/
@Data
public class LoginUser {

	/**
	 * id
	 *
	 * @since 2024/06/29
	 */
	private Long id;

	/**
	 * 创建时间
	 *
	 * @since 2024/06/29
	 */
	private Date createTime;

	/**
	 * 用户名
	 *
	 * @since 2024/06/29
	 */
	private String username;

	/**
	 * 昵称
	 *
	 * @since 2024/06/29
	 */
	private String nickname;

	/**
	 * 性别
	 *
	 * @since 2024/06/29
	 */
	private String gender;

	/**
	 * 头像
	 *
	 * @since 2024/06/29
	 */
	private String avatar;

	/**
	 * 状态(USER_STATUS)
	 *
	 * @since 2024/06/29
	 */
	private String status;

	/**
	 * 密码
	 *
	 * @since 2024/06/29
	 */
	private String password;

	/**
	 * 角色编码集合
	 *
	 * @since 2024/07/05
	 */
	private List<String> roles;

	/**
	 * 路由id集合
	 *
	 * @since 2024/07/20
	 */
	private List<Long> roleIds;

	/**
	 * 权限标识集合
	 *
	 * @since 2024/07/05
	 */
	private List<String> perms;

	private String clientId;

	/**
	 * 用户权限(最大权限)
	 * @since 2025/12/26
	 */

	private DataScopeType dataScopeType;


	/**
	 * 权限部门
	 * @author luyanan
	 * @since 2026/1/3
	 */

	private List<Long> dataScopeDeptIds;


	/**
	 * 部门id
	 * @since 2025/12/26
	 */

	private Long deptId;

	/**
	 * 获取密码
	 *
	 * @return 密码
	 * @since 2024/12/19
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * 获取用户名
	 *
	 * @return 用户名
	 * @since 2024/12/19
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * 账户是否未过期
	 *
	 * @return 是否未过期
	 * @since 2024/12/19
	 */
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 * 账户是否未锁定
	 *
	 * @return 是否未锁定
	 * @since 2024/12/19
	 */
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 * 凭证是否未过期
	 *
	 * @return 是否未过期
	 * @since 2024/12/19
	 */
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * 账户是否启用
	 *
	 * @return 是否启用
	 * @since 2024/12/19
	 */
	public boolean isEnabled() {
		return "0".equals(this.status);
	}
}
