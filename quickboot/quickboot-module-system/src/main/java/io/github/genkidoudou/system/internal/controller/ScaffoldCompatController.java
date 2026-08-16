package io.github.genkidoudou.system.internal.controller;

import java.util.List;
import java.util.Map;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import io.github.genkidoudou.system.internal.service.ISysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * quick-ui 脚手架兼容接口：动态路由。
 */
@Tag(name = "脚手架兼容")
@RequiredArgsConstructor
@RestController
public class ScaffoldCompatController {

  private final ISysPermissionService permissionService;

  /**
   * 若依形态动态路由：按当前用户角色从 sys_menu 组装。
   *
   * @return 路由树（Map 结构与前端脚手架契约对齐）；未登录或无 userId 时返回空列表
   */
  @Operation(summary = "获取动态路由")
  @GetMapping("/getRouters")
  public R<List<Map<String, Object>>> getRouters() {
    StpUtil.checkLogin();
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    String userId = loginUser == null || loginUser.getUserId() == null
      ? null
      : String.valueOf(loginUser.getUserId());
    if (StrUtil.isBlank(userId)) {
      return R.ok(List.of());
    }
    return R.ok(permissionService.buildRouters(userId));
  }
}
