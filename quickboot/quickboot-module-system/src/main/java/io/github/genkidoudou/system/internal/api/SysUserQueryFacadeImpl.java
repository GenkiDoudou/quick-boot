package io.github.genkidoudou.system.internal.api;

import io.github.genkidoudou.system.api.SysUserQueryFacade;
import io.github.genkidoudou.system.api.SysUserView;
import io.github.genkidoudou.system.internal.entity.SysUser;
import io.github.genkidoudou.system.internal.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@link SysUserQueryFacade} 实现：委托现有用户查询能力。
 */
@Service
@RequiredArgsConstructor
public class SysUserQueryFacadeImpl implements SysUserQueryFacade {

  private final ISysUserService userService;

  @Override
  public SysUserView findByUserName(String username) {
    return toView(userService.findByUserName(username));
  }

  @Override
  public SysUserView findByUserId(Long userId) {
    return toView(userService.findByUserId(userId));
  }

  private static SysUserView toView(SysUser user) {
    if (user == null) {
      return null;
    }
    return new SysUserView(
      user.getUserId(),
      user.getUserName(),
      user.getNickName(),
      user.getStatus(),
      user.getDeptId()
    );
  }
}
