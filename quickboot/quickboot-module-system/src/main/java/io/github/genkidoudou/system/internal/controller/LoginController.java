package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.oauth.OauthClientVo;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.system.internal.entity.SysUser;
import io.github.genkidoudou.system.internal.service.ILoginService;
import io.github.genkidoudou.system.internal.service.ISysPermissionService;
import io.github.genkidoudou.system.internal.service.ISysUserService;
import io.github.genkidoudou.system.internal.vo.AuthMeVo;
import io.github.genkidoudou.system.internal.vo.LoginRequestVo;
import io.github.genkidoudou.system.internal.vo.LoginTokenVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录与当前用户。
 *
 * @author luyanan
 * @since 2026/7/25
 */
@Tag(name = "登录认证")
@Slf4j
@RequiredArgsConstructor
@RestController
public class LoginController {

  private final ILoginService loginService;

  private final ISysUserService sysUserService;

  private final ISysPermissionService permissionService;

  /**
   * 应用根路径欢迎页（无鉴权）。
   *
   * @return 固定欢迎文案
   */
  @Operation(summary = "首页")
  @GetMapping("/")
  public String index() {
    log.debug("进入首页");
    return "欢迎";
  }

  /**
   * 账号密码登录（验证码 uuid 在启用天爱时必填）。
   *
   * @param loginRequestVo 用户名、密码及可选验证码
   * @param request        用于提取客户端 IP 等登录上下文
   * @return 访问令牌及过期信息
   */
  @Operation(summary = "账号密码登录")
  @PostMapping("/login")
  public R<LoginTokenVo> login(@RequestBody @Validated(AddGroup.class) LoginRequestVo loginRequestVo, HttpServletRequest request) {

    log.debug("用户名：{}", loginRequestVo.getUsername());
    return R.ok(loginService.login(loginRequestVo, request));
  }

  /**
   * 当前登录用户（quick-ui {@code getInfo} → {@code /auth/me}）。
   *
   * @return 用户基本信息、角色键与权限标识集合；未登录时由 Sa-Token 拦截
   */
  @Operation(summary = "当前登录用户")
  @GetMapping("/auth/me")
  public R<AuthMeVo> me() {
    StpUtil.checkLogin();
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    AuthMeVo vo = new AuthMeVo();
    if (loginUser != null) {
      vo.setUserId(loginUser.getUserId() == null ? null : String.valueOf(loginUser.getUserId()));
      vo.setUsername(loginUser.getUsername());
      vo.setNickName(loginUser.getNickName());
      String uid = vo.getUserId();
      if (uid != null) {
        vo.setRoles(permissionService.listRoleKeys(uid));
        vo.setPermissions(new java.util.ArrayList<>(permissionService.listPermissions(uid)));
      }
    }
    return R.ok(vo);
  }
}
