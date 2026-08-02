package io.github.genkidoudou.system.service.impl;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.plugins.secondary.SecondaryVerificationApplication;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.captcha.CaptchaProperties;
import io.github.genkidoudou.common.crypto.PasswordCodec;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.oauth.OauthClientVo;
import io.github.genkidoudou.common.oauth.utils.OauthClientUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import io.github.genkidoudou.core.entity.enums.CommonEnums;
import io.github.genkidoudou.core.entity.security.LoginHelper;
import io.github.genkidoudou.system.entity.SysUser;
import io.github.genkidoudou.system.service.*;
import io.github.genkidoudou.system.support.LoginLockSupport;
import io.github.genkidoudou.system.vo.LoginRequestVo;
import io.github.genkidoudou.system.vo.LoginTokenVo;
import io.github.genkidoudou.system.vo.SysMenuVo;
import io.github.genkidoudou.system.vo.SysRoleVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 账号密码登录：验证码二次校验、锁定、状态、sa-token 发牌。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements ILoginService {

  private final ISysUserService sysUserService;
  private final PasswordCodec passwordCodec;
  private final LoginLockSupport loginLockSupport;
  private final CaptchaProperties captchaProperties;
  private final ObjectProvider<ImageCaptchaApplication> imageCaptchaApplicationProvider;


  private final ISysPermissionService sysPermissionService;

  @Override
  public LoginTokenVo login(LoginRequestVo request, HttpServletRequest httpServletRequest) {
    Object oauthVo = httpServletRequest.getAttribute(OauthClientVo.ATTR_KEY);
    if (!(oauthVo instanceof OauthClientVo oauthClientVo)) {
      throw new WarningException(600);
    }

    String username = StrUtil.trim(request.getUsername());
    verifyCaptchaIfRequired(request.getUuid());

    if (loginLockSupport.isLocked(username)) {
      long ttl = loginLockSupport.lockTtlSeconds(username);
      if (ttl > 0) {
        throw new WarningException(ErrorCodes.Auth.ACCOUNT_LOCKED, ttl);
      }
      throw new WarningException(ErrorCodes.Auth.ACCOUNT_LOCKED_GENERIC);
    }

    SysUser user = sysUserService.findByUserName(username);
    if (user == null || !passwordCodec.matches(request.getPassword(), user.getPassword())) {
      boolean lockedNow = loginLockSupport.recordFailure(username);
      if (lockedNow) {
        throw new WarningException(ErrorCodes.Auth.ACCOUNT_LOCKED_BY_RETRY);
      }
      throw new WarningException(ErrorCodes.Auth.CREDENTIALS_INVALID);
    }

    if (!CommonEnums.STATUS_ENABLE.getValue().equals(user.getStatus())) {
      throw new WarningException(ErrorCodes.Auth.ACCOUNT_DISABLED);
    }
    loginLockSupport.clear(username);


    // 查询权限列表
    LoginHelper.loginByDevice(buildLoginUser(user, oauthClientVo), oauthClientVo);
    String tokenValue = StpUtil.getTokenValue();
    log.debug("user {} login ok,oauthClient:{}", username, oauthClientVo.getClientId());
    return new LoginTokenVo(tokenValue, StpUtil.getTokenName());
  }

  private LoginUser buildLoginUser(SysUser user, OauthClientVo oauthClientVo) {
    LoginUser loginUser = new LoginUser();
    loginUser.setUserId(user.getUserId());
    loginUser.setUsername(user.getUserName());
    loginUser.setNickName(user.getNickName());
    loginUser.setDeptId(user.getDeptId());
    loginUser.setClientId(oauthClientVo.getClientId());
    List<String> roleKeys = sysPermissionService.listRoleKeys(user.getUserId() + "");
    Set<String> permissions = sysPermissionService.listPermissions(user.getUserId() + "");
    loginUser.setMenuPermission(permissions);
    loginUser.setRolePermission(new HashSet<>(roleKeys));
    return loginUser;
  }

  /**
   * 仅当 {@code qc.captcha.enabled=true} 时做二次校验；关闭则完全跳过。
   */
  private void verifyCaptchaIfRequired(String uuid) {
    if (!OauthClientUtils.isEnable()) {
      return;
    }
    ImageCaptchaApplication application = imageCaptchaApplicationProvider.getIfAvailable();
    if (application == null) {
      throw new WarningException(ErrorCodes.Auth.CAPTCHA_SERVICE_UNAVAILABLE);
    }
    if (!(application instanceof SecondaryVerificationApplication secondary)) {
      throw new WarningException(ErrorCodes.Auth.CAPTCHA_SECONDARY_NOT_CONFIGURED);
    }
    if (StrUtil.isBlank(uuid)) {
      throw new WarningException(ErrorCodes.Auth.CAPTCHA_REQUIRED);
    }
    if (!secondary.secondaryVerification(uuid)) {
      throw new WarningException(ErrorCodes.Auth.CAPTCHA_INVALID);
    }
  }


}
