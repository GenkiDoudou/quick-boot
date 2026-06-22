package io.github.genkidoudou.auth.oauth2.server;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.plugins.secondary.SecondaryVerificationApplication;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.system.oauthclient.service.AuthLoginService;
import io.github.genkidoudou.core.service.LoginLockService;
import io.github.genkidoudou.auth.SysLogininforLogService;
import io.github.genkidoudou.web.system.user.datascope.LoginDataScopeService;
import io.github.genkidoudou.web.system.user.authcache.UserAuthCacheService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * OAuth2 授权服务器登录页桥接：复用主登录校验、验证码与账号锁定。
 */
@Service
@RequiredArgsConstructor
public class OAuth2LoginBridgeService {

    private final AuthLoginService authLoginService;
    private final LoginLockService loginLockService;
    private final LoginDataScopeService loginDataScopeService;
    private final UserAuthCacheService userAuthCacheService;
    private final SysLogininforLogService sysLogininforLogService;
    private final ObjectProvider<ImageCaptchaApplication> imageCaptchaApplicationProvider;
    private final HttpServletRequest request;

    @Value("${qc.login.captcha-enabled:true}")
    private boolean loginCaptchaEnabled;

    /**
     * 供 {@code SaOAuth2Strategy.doLoginHandle} 调用。
     *
     * @param username 登录名
     * @param password 密码
     * @param captchaId  验证码 id（可选，取决于配置）
     * @return Sa-Token 约定 {@link SaResult}
     */
    public Object doOAuth2Login(String username, String password, String captchaId) {
        String name = loginLockService.normalizeUserName(username);
        if (name.isEmpty()) {
            return SaResult.error("用户名或密码错误");
        }
        if (loginCaptchaEnabled) {
            ImageCaptchaApplication app = imageCaptchaApplicationProvider.getIfAvailable();
            if (app == null) {
                return SaResult.error("验证码服务未启用");
            }
            if (StrUtil.isBlank(captchaId)) {
                return SaResult.error("请先完成验证码验证");
            }
            if (!((SecondaryVerificationApplication) app).secondaryVerification(captchaId)) {
                return SaResult.error("验证码已失效，请重试");
            }
        }
        try {
            loginLockService.assertNotLocked(name);
            long userId = authLoginService.authenticate(name, password);
            loginLockService.onLoginSuccess(name);
            StpUtil.login(userId);
            loginDataScopeService.refreshSession(userId);
            userAuthCacheService.refreshSessionOnLogin(userId);
            sysLogininforLogService.recordSuccess(request, userId, name);
            return SaResult.ok().set("loginId", userId);
        } catch (Exception ex) {
            loginLockService.recordFailure(name);
            sysLogininforLogService.recordFailure(request, name, "OAuth2-AS: " + ex.getMessage());
            return SaResult.error(ex.getMessage() != null ? ex.getMessage() : "登录失败");
        }
    }
}
