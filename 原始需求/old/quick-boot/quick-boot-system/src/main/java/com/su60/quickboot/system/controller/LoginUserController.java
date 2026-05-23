package com.su60.quickboot.system.controller;

import com.su60.quickboot.core.security.LoginUser;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.su60.quickboot.system.dos.RouterVo;
import com.su60.quickboot.system.dos.SysUserDo;
import com.su60.quickboot.system.dos.SysUserInfoVo;
import com.su60.quickboot.system.service.ISysMenuService;
import com.su60.quickboot.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 登录用户
 *
 * @author luyanan
 * @since 2024/8/7
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class LoginUserController {


	private final ISysUserService sysUserService;

	private final ISysMenuService sysMenuService;

	/**
	 * 获取当前登陆人的信息
	 *
	 * @return  当前登陆人的信息
	 * @since 2024/8/7
	 */
	@SaCheckPermission("system:user:query")
	@GetMapping("getInfo")
	public SysUserInfoVo getInfo() {
		LoginUser user = LoginUserUtils.getUser();
		if (user == null) {
			throw new RuntimeException("用户未登录");
		}
		SysUserDo sysUserDo = sysUserService.getVoById(user.getId());
		sysUserDo.setPassword(null);
		return new SysUserInfoVo()
				.setUser(sysUserDo)
				.setPermissions(user.getPerms())
				.setRoles(user.getRoles());
	}


	/**
	 * 获取当前登录人的路由信息
	 *
	 * @return 当前登录人的路由信息
	 * @since 2024/8/7
	 */
	@SaCheckPermission("system:menu:list")
	@GetMapping("getRouters")
	public List<RouterVo> getRouters() {
		LoginUser user = LoginUserUtils.getUser();
		if (user == null) {
			throw new RuntimeException("用户未登录");
		}
		return sysMenuService.getRouters(user.getId());
	}
}
