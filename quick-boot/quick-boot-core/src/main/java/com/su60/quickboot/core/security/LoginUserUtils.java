package com.su60.quickboot.core.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * 当前登陆人工具类
 *
 * @author luyanan
 * @since 2024/07/07
 **/
@Slf4j
@UtilityClass
public class LoginUserUtils {


	/**
	 * 获取当前登录的用户信息
	 *
	 * @return 当前登录的用户信息
	 * @since 2024/07/07
	 */
	public LoginUser getUser() {
		try {
			if (StpUtil.isLogin()) {
				Object user = StpUtil.getExtra("user");
				LoginUser loginUser =JSON.parseObject(user.toString(), LoginUser.class);
				if (null != loginUser) {
					return loginUser;
				}
				// 暂时返回null，避免模块依赖问题
				// 实际使用时需要从JWT中解析用户信息
				return null;
			}
		} catch (Exception e) {
			// 忽略异常，返回null
		}
		return null;
	}


	/**
	 * 获取当前登录用户ID
	 *
	 * @return 当前登录用户ID
	 * @since 2024/12/19
	 */
	public Long getUserId() {
		try {
			if (StpUtil.isLogin()) {
				return StpUtil.getLoginIdAsLong();
			}
		} catch (Exception e) {
			// 忽略异常，返回null
		}
		return null;
	}

	/**
	 * 是否为admin
	 *
	 * @param userId 用户id
	 * @return 当前用户是否为admin
	 * @since 2024/8/7
	 */
	public boolean isAdmin(Long userId) {
		return null != userId && 1L == userId;
	}

	/**
	 * 是否为admin（当前用户）
	 *
	 * @return 当前用户是否为admin
	 * @since 2024/12/19
	 */
	public boolean isAdmin() {
		Long userId = getUserId();
		return isAdmin(userId);
	}
}
