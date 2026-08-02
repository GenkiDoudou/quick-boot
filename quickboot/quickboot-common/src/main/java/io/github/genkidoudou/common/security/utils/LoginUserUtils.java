package io.github.genkidoudou.common.security.utils;

import cn.hutool.extra.spring.SpringUtil;
import io.github.genkidoudou.common.security.service.LoginUserService;
import io.github.genkidoudou.common.security.vo.LoginUser;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LoginUserUtils {

  /**
   * 获取token的header
   *
   * @since 2026/7/29
   */

  public static final String TOKEN_HEADER = "Authorization";
  public static final String LOGIN_USER_KEY = "loginUser";

  public static final String BASIC = "Basic ";

  /**
   * 获取登录用户
   *
   * @return
   * @since 2026/7/29
   */
  public LoginUser getLoginUser() {
    try {
      LoginUserService loginUserService = SpringUtil.getBean(LoginUserService.class);
      return loginUserService == null ? null : loginUserService.getLoginUser();
    } catch (Throwable ignored) {
      // 尚未接入登录实现（如 sa-token）时视为未登录
      return null;
    }
  }


}
