package com.su60.quickboot.web.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.common.utils.IpUtils;
import com.su60.quickboot.common.utils.ServletUtil;
import com.su60.quickboot.core.security.LoginUser;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.su60.quickboot.common.security.PasswordEncoder;
import com.su60.quickboot.data.datascope.DataScopeType;
import com.su60.quickboot.data.spring.SpringContextHolder;
import com.su60.quickboot.system.dos.SysLogininforDo;
import com.su60.quickboot.system.dos.SysRoleDo;
import com.su60.quickboot.system.dos.SysUserDo;
import com.su60.quickboot.system.entity.SysRoleEntity;
import com.su60.quickboot.system.service.*;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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

	private final ISysRoleDeptService sysRoleDeptService;

	private final ISysDeptService sysDeptService;

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


			// 权限
			// TODO 这里是不是可以考虑一下 不放这里 而是直接将角色放缓存里面
			// 多角色取权限最大的
//			DataScopeType scope = sysRoleDos.stream()
//					.filter(a -> StrUtil.isNotBlank(a.getDataScope()))
//					.map(r -> DataScopeType.valueOf(r.getDataScope()))
//					.max(Comparator.comparingInt(DataScopeType::getPriority))
//					.orElse(DataScopeType.SELF);
//			loginUser.setDataScopeType(scope);

			loginUser.setDeptId(sysUserDo.getDeptId());


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

			// 这里处理权限的问题
			List<SysRoleEntity> roleEntities = this.sysRoleService.getVoByIds(new HashSet<>(roleIds));
			DataScopeType dataScopeType = null;
			//  1。 如果包含全部数据权限, 则按照最大的权限来
			//  2.  如果包含自己的权限,  并且 没有其他权限, 则 就是自己
			List<Long> deptIds = new ArrayList<>();
			if (roleEntities.stream().anyMatch(a -> a.getDataScope().equals("1"))) {
				dataScopeType = DataScopeType.ALL;
			} else {
				if (roleEntities.size() == 1 && roleEntities.get(0).getDataScope().equals("5")) {
					dataScopeType = DataScopeType.SELF;
				} else {

					// 获取部门id

					// 先处理自定义的权限
					List<Long> customRoleIds = roleEntities.stream().filter(a -> a.getDataScope().equals("2")).distinct().map(SysRoleEntity::getId).collect(Collectors.toList());
					List<Long> customDeptIds = sysRoleDeptService.listDeptByRoleIds(customRoleIds);
					deptIds.addAll(customDeptIds);
					// 本部门权限
					if (roleEntities.stream().anyMatch(a -> a.getDataScope().equals("3"))) {
						deptIds.add(sysUserDo.getDeptId());
					}
					// 本部门以及以下部门数据权限
					if (roleEntities.stream().anyMatch(a -> a.getDataScope().equals("4"))) {
						List<Long> longs = sysDeptService.listDeptAndChild(sysUserDo.getDeptId());
						deptIds.addAll(longs);
					}
					dataScopeType = DataScopeType.DEPT;
				}
			}

			loginUser.setDataScopeType(dataScopeType);
			loginUser.setDataScopeDeptIds(deptIds.stream().distinct().collect(Collectors.toList()));
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
			if (null != user) {
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
