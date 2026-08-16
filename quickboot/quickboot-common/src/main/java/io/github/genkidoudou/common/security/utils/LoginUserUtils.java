package io.github.genkidoudou.common.security.utils;

import cn.hutool.extra.spring.SpringUtil;
import io.github.genkidoudou.common.security.service.LoginUserService;
import io.github.genkidoudou.common.security.vo.LoginUser;
import lombok.experimental.UtilityClass;

/**
 * 登录用户上下文工具：从 Spring 容器解析 {@link LoginUserService}。
 */
@UtilityClass
public class LoginUserUtils {

  /** HTTP 请求头：Bearer / Basic 认证均使用该头。 */
  public static final String TOKEN_HEADER = "Authorization";

  /** sa-token 会话中缓存 {@link LoginUser} 的键名。 */
  public static final String LOGIN_USER_KEY = "loginUser";

  /** Client Basic 认证 scheme 前缀（含尾部空格）。 */
  public static final String BASIC = "Basic ";

  /**
   * 获取当前登录用户。
   * <p>
   * 尚未接入 {@link LoginUserService} 或 Bean 不可用时返回 {@code null}，不抛异常。
   *
   * @return 登录用户或 {@code null}
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
