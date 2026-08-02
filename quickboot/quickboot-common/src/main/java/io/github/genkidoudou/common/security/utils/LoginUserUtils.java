package io.github.genkidoudou.auth.utils;

import cn.hutool.extra.spring.SpringUtil;
import io.github.genkidoudou.auth.service.LoginUserService;
import io.github.genkidoudou.auth.vo.LoginUser;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LoginUserUtils {

  /**
   * 获取token的header
   *
   * @since 2026/7/29
   */

  public static final String TOKEN_HEADER = "Authorization";


  /**
   * 获取登录用户
   *
   * @return
   * @since 2026/7/29
   */
  public LoginUser getLoginUser() {
    return SpringUtil.getBean(LoginUserService.class).getLoginUser();
  }
}
