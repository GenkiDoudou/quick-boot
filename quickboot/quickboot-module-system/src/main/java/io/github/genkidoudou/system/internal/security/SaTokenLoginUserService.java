package io.github.genkidoudou.system.internal.security;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.security.service.LoginUserService;
import io.github.genkidoudou.common.security.vo.LoginUser;
import io.github.genkidoudou.core.entity.security.LoginHelper;
import io.github.genkidoudou.system.internal.entity.SysUser;
import io.github.genkidoudou.system.internal.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 基于 sa-token 的当前用户解析。
 * <p>
 * 优先读取登录时写入 TokenSession 的 {@link LoginUser}（含 {@code clientId}），
 * 再用库表用户信息补齐展示字段；{@code clientId} 仍空时回退登录设备类型
 * （{@link LoginHelper#loginByDevice} 写入的即为 OAuth clientId）。
 */
@Service
@RequiredArgsConstructor
public class SaTokenLoginUserService implements LoginUserService {

  private final ISysUserService sysUserService;

  @Override
  public LoginUser getLoginUser() {
    if (!StpUtil.isLogin()) {
      return null;
    }
    Long userId = StpUtil.getLoginIdAsLong();
    LoginUser sessionUser = readSessionLoginUser();
    SysUser user = sysUserService.findByUserId(userId);

    LoginUser loginUser = sessionUser != null ? sessionUser : new LoginUser();
    loginUser.setUserId(userId);
    if (user != null) {
      loginUser.setUsername(user.getUserName());
      loginUser.setNickName(user.getNickName());
      loginUser.setDeptId(user.getDeptId());
    } else if (sessionUser == null) {
      // 仅有登录态、无用户行、无会话缓存时返回最小 stub
      return loginUser;
    }
    if (StrUtil.isBlank(loginUser.getClientId())) {
      loginUser.setClientId(resolveClientIdFromDevice());
    }
    return loginUser;
  }

  private static LoginUser readSessionLoginUser() {
    try {
      Object raw = StpUtil.getTokenSession().get(LoginHelper.LOGIN_USER_KEY);
      if (raw instanceof LoginUser lu) {
        return lu;
      }
    } catch (Exception ignored) {
      // session 不可用时忽略
    }
    return null;
  }

  /**
   * {@link LoginHelper#loginByDevice} 将 OAuth clientId 写入 deviceType。
   */
  private static String resolveClientIdFromDevice() {
    try {
      String device = StpUtil.getLoginDeviceType();
      if (StrUtil.isNotBlank(device)) {
        return device.trim();
      }
    } catch (Exception ignored) {
      // API 差异或未设置 device 时忽略
    }
    return null;
  }
}
