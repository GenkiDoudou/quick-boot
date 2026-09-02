package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import io.github.genkidoudou.system.internal.service.ISysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 动态菜单路由：按当前登录用户角色从 {@code sys_menu} 组装前端路由树。
 */
@Tag(name = "菜单路由")
@RequiredArgsConstructor
@RestController
public class MenuRouteController {

  private final ISysPermissionService permissionService;

  /**
   * 当前用户可访问的动态路由树（quick-ui 菜单挂载）。
   *
   * @return 路由 Map 列表；未登录或无 userId 时为空列表
   */
  @Operation(summary = "获取动态菜单路由")
  @GetMapping("/api/menu/routes")
  public R<List<Map<String, Object>>> menuRoutes() {
    return R.ok(resolveRoutersForCurrentUser());
  }

  /**
   * 兼容旧路径 {@code /getRouters}；后续版本将移除。
   *
   * @return 与 {@link #menuRoutes()} 相同
   * @deprecated 请改用 GET {@code /api/menu/routes}
   */
  @Deprecated
  @Operation(summary = "获取动态路由（兼容）", deprecated = true)
  @GetMapping("/getRouters")
  public R<List<Map<String, Object>>> getRoutersCompat() {
    return menuRoutes();
  }

  private List<Map<String, Object>> resolveRoutersForCurrentUser() {
    StpUtil.checkLogin();
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    String userId = loginUser == null || loginUser.getUserId() == null
      ? null
      : String.valueOf(loginUser.getUserId());
    if (StrUtil.isBlank(userId)) {
      return List.of();
    }
    return permissionService.buildRouters(userId);
  }
}
