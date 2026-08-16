package io.github.genkidoudou.common.security.service;

import io.github.genkidoudou.common.security.vo.LoginUser;

/**
 * 当前登录用户 SPI：由 sa-token 等安全模块提供实现。
 */
public interface LoginUserService {

  /**
   * 获取当前请求的登录用户。
   *
   * @return 已登录用户信息；未登录或未装配实现时返回 {@code null}
   */
  LoginUser getLoginUser();




}
