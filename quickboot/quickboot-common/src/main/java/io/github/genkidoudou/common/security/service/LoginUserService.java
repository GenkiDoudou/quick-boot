package io.github.genkidoudou.auth.service;

import io.github.genkidoudou.auth.vo.LoginUser;

public interface LoginUserService {


  /**
   * 获取登录用户
   *
   * @return
   * @since 2026/7/29
   */
  LoginUser getLoginUser();

}
