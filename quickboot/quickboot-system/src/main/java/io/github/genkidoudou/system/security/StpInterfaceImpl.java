package io.github.genkidoudou.system.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * sa-token 权限/角色数据源：对接 {@link ISysPermissionService}，结果缓存在 Account-Session。
 * <p>角色菜单或用户角色变更后须调用 {@link SaPermissionCache} 失效，否则会读到旧权限。</p>
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

  private final ISysPermissionService permissionService;

  @Override
  public List<String> getPermissionList(Object loginId, String loginType) {
    String userId = loginId == null ? null : String.valueOf(loginId);
    if (StrUtil.isBlank(userId)) {
      return Collections.emptyList();
    }
    SaSession session = StpUtil.getSessionByLoginId(loginId);
    return session.get(SaPermissionCache.KEY_PERMISSION_LIST,
      () -> new ArrayList<>(permissionService.listPermissions(userId)));
  }

  @Override
  public List<String> getRoleList(Object loginId, String loginType) {
    String userId = loginId == null ? null : String.valueOf(loginId);
    if (StrUtil.isBlank(userId)) {
      return Collections.emptyList();
    }
    SaSession session = StpUtil.getSessionByLoginId(loginId);
    return session.get(SaPermissionCache.KEY_ROLE_LIST,
      () -> new ArrayList<>(permissionService.listRoleKeys(userId)));
  }
}
