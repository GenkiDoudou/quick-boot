package com.su60.quickboot.web.config;

import cn.dev33.satoken.stp.StpInterface;
import com.su60.quickboot.core.security.LoginUser;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.su60.quickboot.system.dos.SysRoleDo;
import com.su60.quickboot.system.service.ISysMenuService;
import com.su60.quickboot.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限认证接口实现类
 *
 * @author luyanan
 * @since 2024/12/19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaTokenPermissionConfiguration implements StpInterface {

	private final ISysRoleService sysRoleService;

	private final ISysMenuService sysMenuService;

	/**
	 * 返回一个账号所拥有的权限码集合
	 */
	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		LoginUser user = LoginUserUtils.getUser();
		List<String> perms = user.getPerms();

//		try {
//			// 查询角色
//			List<SysRoleDo> sysRoleDos = sysRoleService.listByUserId(Long.valueOf(loginId.toString()));
//			List<Long> roleIds = sysRoleDos.stream().map(SysRoleDo::getId).toList();
//			return sysMenuService.listPermsByRoleIds(roleIds);
//		} catch (Exception e) {
//			log.error("获取用户权限列表失败", e);
//		}
//		return List.of();
		return perms;
	}

	/**
	 * 返回一个账号所拥有的角色标识集合
	 */
	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
//		try {
//			// 查询角色
//			List<SysRoleDo> sysRoleDos = sysRoleService.listByUserId(Long.valueOf(loginId.toString()));
//			return sysRoleDos.stream().map(SysRoleDo::getRoleKey).toList();
//		} catch (Exception e) {
//			log.error("获取用户角色列表失败", e);
//		}
//		return List.of();
		LoginUser user = LoginUserUtils.getUser();
		List<String> roles = user.getRoles();
		return roles;
	}
}
