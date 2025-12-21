package com.su60.quickboot.web.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.common.utils.IpUtils;
import com.su60.quickboot.common.utils.ServletUtil;
import com.su60.quickboot.core.security.LoginUser;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.su60.quickboot.common.security.PasswordEncoder;
import com.su60.quickboot.data.spring.SpringContextHolder;
import com.su60.quickboot.system.dos.SysLogininforDo;
import com.su60.quickboot.system.dos.SysRoleDo;
import com.su60.quickboot.system.dos.SysUserDo;
import com.su60.quickboot.system.service.ISysLogininforService;
import com.su60.quickboot.system.service.ISysMenuService;
import com.su60.quickboot.system.service.ISysRoleService;
import com.su60.quickboot.system.service.ISysUserService;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Sa-Token 登录服务
 *
 * @author luyanan
 * @since 2024/12/19
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class SaTokenLoginService {

	private final ISysUserService sysUserService;

	private final PasswordEncoder passwordEncoder;
	private final ISysRoleService sysRoleService;

	private final ISysMenuService sysMenuService;

	private final ISysLogininforService logininforService;

	/**
	 * 用户登录
	 *
	 * @param username 用户名
	 * @param password 密码
	 * @return 登录结果
	 */
	public String login(String username, String password) {
		String loginResult = "0";
		try {
			// 1. 根据用户名查找用户
			SysUserDo sysUserDo = sysUserService.findByUserName(username);
			if (sysUserDo == null) {
				throw new RuntimeException("用户名或密码错误");
			}

			// 2. 验证密码
			if (!passwordEncoder.matches(password, sysUserDo.getPassword())) {
				throw new RuntimeException("用户名或密码错误");
			}

			// 3. 检查用户状态
			if ("1".equals(sysUserDo.getStatus())) {
				throw new RuntimeException("用户已被禁用");
			}

			LoginUser loginUser = BeanConvertUtils.convertTo(sysUserDo, LoginUser::new);
			loginUser.setUsername(sysUserDo.getUserName());
			loginUser.setNickname(sysUserDo.getNickName());
			loginUser.setGender(sysUserDo.getSex());
			loginUser.setAvatar(sysUserDo.getAvatar());
			loginUser.setStatus(sysUserDo.getStatus());
			// 查询角色
			List<SysRoleDo> sysRoleDos = sysRoleService.listByUserId(sysUserDo.getId());
			List<Long> roleIds = sysRoleDos.stream().map(SysRoleDo::getId).toList();
			// 角色编码
			List<String> roleKeys = sysRoleDos.stream().map(SysRoleDo::getRoleKey).toList();

			loginUser.setRoleIds(roleIds);
			loginUser.setRoles(roleKeys);
			// 查询菜单权限
			List<String> perms = sysMenuService.listPermsByRoleIds(roleIds);
//		if (loginUser.isAdmin()) {
//			perms.add("*:*:*");
//		}
			loginUser.setPerms(perms);
			// 4. 执行登录
			StpUtil.login(sysUserDo.getId(), SaLoginConfig.setExtra("user", loginUser));


			// 5. 返回token
			return StpUtil.getTokenValue();

		} catch (Exception e) {
			loginResult = "1";
			log.error("用户登录失败: {}", e.getMessage(), e);
			throw new RuntimeException("登录失败: " + e.getMessage());
		} finally {
			SysLogininforDo sysLogininforDo = new SysLogininforDo();
			sysLogininforDo.setUserId(LoginUserUtils.getUserId());
			sysLogininforDo.setUserName(username);
			sysLogininforDo.setIpaddr(ServletUtil.getClientIP(SpringContextHolder.getRequest()));
			sysLogininforDo.setLoginLocation(IpUtils.getRegion(sysLogininforDo.getIpaddr()));
			// 获取浏览器
			final UserAgent userAgent = UserAgent.parseUserAgentString(SpringContextHolder.getRequest().getHeader("User-Agent"));
			sysLogininforDo.setBrowser(userAgent.getBrowser().getName());
			sysLogininforDo.setOs(userAgent.getOperatingSystem().getName());
			sysLogininforDo.setLoginTime(new Date());
			sysLogininforDo.setLoginType("login");
			sysLogininforDo.setLoginResult(loginResult);
			// 获取操作系统
			// 记录登录日志
			logininforService.saveLog(sysLogininforDo);
		}
	}

	/**
	 * 用户登出
	 */
	public void logout() {
		String loginResult = "0";
		LoginUser user = LoginUserUtils.getUser();
		try {

			StpUtil.logout();
		} catch (Exception e) {
			loginResult = "1";
			log.error("用户登出失败", e);
		} finally {
			SysLogininforDo sysLogininforDo = new SysLogininforDo();
			sysLogininforDo.setUserId(LoginUserUtils.getUserId());
			sysLogininforDo.setUserName(user.getUsername());
			sysLogininforDo.setIpaddr(ServletUtil.getClientIP(SpringContextHolder.getRequest()));
			sysLogininforDo.setLoginLocation(IpUtils.getRegion(sysLogininforDo.getIpaddr()));
			// 获取浏览器
			final UserAgent userAgent = UserAgent.parseUserAgentString(SpringContextHolder.getRequest().getHeader("User-Agent"));
			sysLogininforDo.setBrowser(userAgent.getBrowser().getName());
			sysLogininforDo.setOs(userAgent.getOperatingSystem().getName());
			sysLogininforDo.setLoginTime(new Date());
			sysLogininforDo.setLoginType("logout");
			sysLogininforDo.setLoginResult(loginResult);
			// 获取操作系统
			// 记录登录日志
			logininforService.saveLog(sysLogininforDo);
		}
	}

	/**
	 * 检查是否已登录
	 *
	 * @return 是否已登录
	 */
	public boolean isLogin() {
		return StpUtil.isLogin();
	}

	/**
	 * 获取当前登录用户ID
	 *
	 * @return 用户ID
	 */
	public Long getLoginUserId() {
		if (StpUtil.isLogin()) {
			return StpUtil.getLoginIdAsLong();
		}
		return null;
	}


}
