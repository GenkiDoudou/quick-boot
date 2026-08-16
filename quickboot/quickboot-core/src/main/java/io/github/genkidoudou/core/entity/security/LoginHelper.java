package io.github.genkidoudou.core.entity.security;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import io.github.genkidoudou.common.oauth.OauthClientVo;
import io.github.genkidoudou.common.security.vo.LoginUser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sa-Token 登录辅助：封装登录、按 OAuth 客户端设备维度登录及 {@link LoginUser} 会话缓存。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {

  /** Token Session 中存储 {@link LoginUser} 的键名。 */
  public static final String LOGIN_USER_KEY = "loginUser";

  /**
   * 登录系统
   *
   * @param loginUser 登录用户信息
   */
  public static void login(LoginUser loginUser) {
    SaHolder.getStorage().set(LOGIN_USER_KEY, loginUser);
    StpUtil.login(loginUser.getUserId());
    setLoginUser(loginUser);
  }

  /**
   * 登录系统（按 OAuth 客户端设备维度）
   * <p>
   * {@code tokenTimeout} 非空时写入本次登录有效期（秒）；为空则沿用 sa-token 全局 timeout。
   *
   * @param loginUser     登录用户信息
   * @param oauthClientVo 当前客户端
   */
  public static void loginByDevice(LoginUser loginUser, OauthClientVo oauthClientVo) {
    SaHolder.getStorage().set(LOGIN_USER_KEY, loginUser);

    SaLoginParameter parameter = new SaLoginParameter()
      .setDeviceType(oauthClientVo.getClientId())
      .setIsLastingCookie(true);
    Long tokenTimeout = oauthClientVo.getTokenTimeout();
    if (tokenTimeout != null && tokenTimeout > 0) {
      parameter.setTimeout(tokenTimeout);
    }
    StpUtil.login(loginUser.getUserId(), parameter);
    setLoginUser(loginUser);
  }

  /**
   * 设置用户数据（写入 Token Session 多级缓存）。
   *
   * @param loginUser 登录用户信息
   */
  public static void setLoginUser(LoginUser loginUser) {
    StpUtil.getTokenSession().set(LOGIN_USER_KEY, loginUser);
  }
}
