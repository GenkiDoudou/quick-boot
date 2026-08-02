package io.github.genkidoudou.system.service;

import io.github.genkidoudou.system.vo.LoginRequestVo;
import io.github.genkidoudou.system.vo.LoginTokenVo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 账号密码登录编排。
 */
public interface ILoginService {

  /**
   * 验证码（可选）→ 锁定 → 凭证/状态 → sa-token 发牌。
   *
   * @param request 登录请求
   * @return token 载荷
   */
  LoginTokenVo login(LoginRequestVo request, HttpServletRequest httpServletRequest);
}
