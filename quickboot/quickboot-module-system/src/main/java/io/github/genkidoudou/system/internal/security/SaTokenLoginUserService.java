package io.github.genkidoudou.system.internal.security;

import cn.dev33.satoken.stp.StpUtil;
import io.github.genkidoudou.common.security.service.LoginUserService;
import io.github.genkidoudou.common.security.vo.LoginUser;
import io.github.genkidoudou.system.internal.entity.SysUser;
import io.github.genkidoudou.system.internal.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 基于 sa-token 的当前用户解析。
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
    SysUser user = sysUserService.findByUserId(userId);
    if (user == null) {
      LoginUser stub = new LoginUser();
      stub.setUserId(userId);
      return stub;
    }
    LoginUser loginUser = new LoginUser();
    loginUser.setUserId(user.getUserId());
    loginUser.setUsername(user.getUserName());
    loginUser.setNickName(user.getNickName());
    return loginUser;
  }
}
