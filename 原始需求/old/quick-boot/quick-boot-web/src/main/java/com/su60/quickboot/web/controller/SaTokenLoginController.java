package com.su60.quickboot.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.su60.quickboot.common.core.R;
import com.su60.quickboot.common.captcha.CaptchaService;
import com.su60.quickboot.common.exception.GlobalMsgCode;
import com.su60.quickboot.common.exception.WarningException;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.su60.quickboot.web.service.SaTokenLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Sa-Token 登录控制器
 *
 * @author luyanan
 * @since 2024/12/19
 */
@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class SaTokenLoginController {

	private final SaTokenLoginService saTokenLoginService;
	private final CaptchaService captchaService;

	/**
	 * 用户登录
	 *
	 * @param username 用户名
	 * @param password 密码
	 * @param code 验证码
	 * @param uuid 验证码唯一标识
	 * @return 登录结果
	 */
	@PostMapping("/login")
	public R<Map<String, Object>> login(@RequestParam String username,
										@RequestParam String password,
										@RequestParam(required = false) String code,
										@RequestParam(required = false) String uuid) {
		// 1. 验证码校验
		if (code != null && uuid != null) {
			boolean captchaValid = captchaService.check(uuid, code);
			if (!captchaValid) {
				log.warn("验证码校验失败: username={}, code={}, uuid={}", username, code, uuid);
				throw new WarningException(GlobalMsgCode.BAD_REQUEST, "验证码错误");
			}
		}

		// 2. 执行登录（异常由全局异常处理器处理）
		String token = saTokenLoginService.login(username, password);

		// 3. 构建返回结果
		Map<String, Object> result = new HashMap<>();
		result.put("access_token", token);
		result.put("tokenTimeout", StpUtil.getTokenTimeout());
		return R.ok(result);
	}

	/**
	 * 用户登录（JSON格式）
	 *
	 * @param loginRequest 登录请求
	 * @return 登录结果
	 */
	@PostMapping("/json")
	public R<Map<String, Object>> loginJson(@RequestBody Map<String, String> loginRequest) {
		String username = loginRequest.get("username");
		String password = loginRequest.get("password");
		String code = loginRequest.get("code");
		String uuid = loginRequest.get("uuid");
		return login(username, password, code, uuid);
	}

	/**
	 * 用户登录（兼容原有接口）
	 *
	 * @param loginRequest 登录请求
	 * @return 登录结果
	 */
	@PostMapping("/auth")
	public R<Map<String, Object>> auth(@RequestBody Map<String, String> loginRequest) {
		String username = loginRequest.get("username");
		String password = loginRequest.get("password");
		String code = loginRequest.get("code");
		String uuid = loginRequest.get("uuid");
		return login(username, password, code, uuid);
	}

	/**
	 * 用户登出
	 *
	 * @return 登出结果
	 */
	@PostMapping("/logout")
	public R<Void> logout() {
		// 异常由全局异常处理器处理
		saTokenLoginService.logout();
		return R.ok();
	}

	/**
	 * 检查登录状态
	 *
	 * @return 登录状态
	 */
	@GetMapping("/check")
	public R<Map<String, Object>> checkLogin() {
		// 异常由全局异常处理器处理
		Map<String, Object> result = new HashMap<>();
		result.put("isLogin", StpUtil.isLogin());
		if (StpUtil.isLogin()) {
			result.put("loginId", StpUtil.getLoginId());
			result.put("loginDevice", StpUtil.getLoginDevice());
			result.put("tokenTimeout", StpUtil.getTokenTimeout());
			result.put("sessionTimeout", StpUtil.getSessionTimeout());
			// Sa-Token 1.37.0 中这些方法可能不存在，暂时注释
			// result.put("loginTime", StpUtil.getLoginTime());
			// result.put("lastActivityTime", StpUtil.getLastActivityTime());
		}
		return R.ok(result);
	}

	/**
	 * 获取当前登录用户信息
	 *
	 * @return 用户信息
	 */
	@GetMapping("/user")
	public R<Object> getCurrentUser() {
		// 异常由全局异常处理器处理
		if (!StpUtil.isLogin()) {
			throw new WarningException(GlobalMsgCode.UNAUTHORIZED, "用户未登录");
		}

		Object user = LoginUserUtils.getUser();
		return R.ok(user);
	}

	/**
	 * 踢人下线
	 *
	 * @param loginId 用户ID
	 * @return 操作结果
	 */
	@PostMapping("/kickout/{loginId}")
	public R<Void> kickout(@PathVariable Long loginId) {
		// 异常由全局异常处理器处理
		StpUtil.kickout(loginId);
		return R.ok();
	}

	/**
	 * 踢人下线（指定设备）
	 *
	 * @param loginId 用户ID
	 * @param device  设备标识
	 * @return 操作结果
	 */
	@PostMapping("/kickout/{loginId}/{device}")
	public R<Void> kickout(@PathVariable Long loginId, @PathVariable String device) {
		// 异常由全局异常处理器处理
		StpUtil.kickout(loginId, device);
		return R.ok();
	}
}
