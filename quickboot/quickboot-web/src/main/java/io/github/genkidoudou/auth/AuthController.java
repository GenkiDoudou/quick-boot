package io.github.genkidoudou.auth;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.plugins.secondary.SecondaryVerificationApplication;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.monitor.operlog.OperLogBusinessType;
import io.github.genkidoudou.common.monitor.operlog.OperLogMeta;
import io.github.genkidoudou.core.service.LoginLockService;
import io.github.genkidoudou.web.system.menu.service.MenuService;
import io.github.genkidoudou.web.system.oauthclient.service.AuthLoginService;
import io.github.genkidoudou.web.system.online.support.OnlineSessionRecorder;
import io.github.genkidoudou.web.system.user.datascope.DataScopeSession;
import io.github.genkidoudou.web.system.user.datascope.DataScopeSessionStore;
import io.github.genkidoudou.web.system.user.datascope.LoginDataScopeService;
import io.github.genkidoudou.web.system.user.authcache.UserAuthCacheService;
import io.github.genkidoudou.web.system.user.authcache.UserAuthSessionStore;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录与会话接口，对接 {@code quick-ui}（{@code /login}、{@code /getInfo}、{@code /getRouters} 等）。
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final MenuService menuService;
    private final AuthLoginService authLoginService;
    private final LoginDataScopeService loginDataScopeService;
    private final UserAuthCacheService userAuthCacheService;
    private final SysUserMapper userMapper;
    private final LoginLockService loginLockService;
    private final SysLogininforLogService sysLogininforLogService;
    private final OnlineSessionRecorder onlineSessionRecorder;

    private final ObjectProvider<ImageCaptchaApplication> imageCaptchaApplicationProvider;

    /** 是否校验登录行为验证码（关闭后 /login 不再校验 captchaId，便于自动化）。 */
    @Value("${qc.login.captcha-enabled:true}")
    private boolean loginCaptchaEnabled;

    /**
     * 供登录页读取：是否与 {@code qc.login.captcha-enabled} 一致启用天爱行为验证码。
     *
     * @return data 中含 {@code captchaEnabled} 布尔
     */
    @Operation(summary = "登录页是否启用行为验证码")
    @GetMapping("/login/captcha-config")
    public R<Map<String, Object>> loginCaptchaConfig() {
        Map<String, Object> payload = new LinkedHashMap<>(2);
        payload.put("captchaEnabled", loginCaptchaEnabled);
        return R.ok(payload);
    }

    /**
     * 账号密码登录。
     *
     * @param username  登录名
     * @param password  密码
     * @param captchaId 天爱验证码二次校验 id（{@code /api/captcha/validate} 成功后返回）；{@code qc.login.captcha-enabled=false} 时可不传
     * @return {@code data.access_token} 供前端存入 Storage/Cookie
     */
    @OperLogMeta(title = "用户登录", businessType = OperLogBusinessType.OTHER, operatorType = 0)
    @PostMapping("/login")
    public R<Map<String, Object>> login(HttpServletRequest request,
                                        @RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam(required = false) String captchaId) {
        String name = loginLockService.normalizeUserName(username);
        if (name.isEmpty()) {
            return R.error(HttpCodes.UNAUTHORIZED, "用户名或密码错误");
        }
        if (loginCaptchaEnabled) {
            ImageCaptchaApplication imageCaptchaApplication = imageCaptchaApplicationProvider.getIfAvailable();
            if (imageCaptchaApplication == null) {
                return R.error(HttpCodes.INTERNAL_ERROR, "验证码服务未启用");
            }
            if (StrUtil.isBlank(captchaId)) {
                return R.error(HttpCodes.UNAUTHORIZED, "请先完成验证码验证");
            }
            if (!((SecondaryVerificationApplication) imageCaptchaApplication).secondaryVerification(captchaId)) {
                return R.error(HttpCodes.UNAUTHORIZED, "验证码已失效，请重试");
            }
        }
        loginLockService.assertNotLocked(name);
        try {
            long userId = authLoginService.authenticate(name, password);
            loginLockService.onLoginSuccess(name);
            StpUtil.login(userId);
            onlineSessionRecorder.record(request, userId);
            loginDataScopeService.refreshSession(userId);
            userAuthCacheService.refreshSessionOnLogin(userId);
            sysLogininforLogService.recordSuccess(request, userId, name);
            Map<String, Object> data = new HashMap<>(2);
            data.put("access_token", StpUtil.getTokenValue());
            return R.ok(data);
        } catch (WarningException ex) {
            if (ex.getCode() == ErrorCodes.Security.UNAUTHORIZED) {
                loginLockService.recordFailure(name);
                sysLogininforLogService.recordFailure(request, name, ex.getMessage());
            }
            throw ex;
        }
    }

    /**
     * 获取当前登录用户信息（前端路由与权限装配依赖）。
     *
     * @return {@code user}、{@code roles}、{@code permissions}
     */
    @GetMapping("/getInfo")
    public R<Map<String, Object>> getInfo() {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        SysUser u = userMapper.selectById(userId);
        if (u == null) {
            return R.error(HttpCodes.NOT_FOUND, "用户不存在或已删除");
        }
        Map<String, Object> user = new LinkedHashMap<>(8);
        user.put("userId", u.getUserId());
        user.put("userName", u.getUserName());
        user.put("nickName", StrUtil.blankToDefault(u.getNickName(), u.getUserName()));
        user.put("avatar", "");
        DataScopeSession scope = DataScopeSessionStore.get();
        user.put("deptId", scope != null ? scope.loginDeptId() : u.getDeptId());

        Map<String, Object> data = new LinkedHashMap<>(4);
        data.put("user", user);
        long authVersion = userAuthCacheService.currentGlobalVersion();
        List<String> roles = UserAuthSessionStore.getRolesIfValid(authVersion);
        List<String> permissions = UserAuthSessionStore.getPermissionsIfValid(authVersion);
        if (roles == null) {
            roles = menuService.listRoleKeysByUserId(userId);
        }
        if (permissions == null) {
            permissions = menuService.listPermissionsByUserId(userId);
        }
        data.put("roles", roles);
        data.put("permissions", permissions);
        return R.ok(data);
    }

    /**
     * 注销当前会话。
     *
     * @return 空载荷成功响应
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        return R.ok();
    }

    /**
     * 动态路由树（若依风格），由菜单与角色权限装配。
     *
     * @return 路由数组，可为空
     */
    @GetMapping("/getRouters")
    @SuppressWarnings("unchecked")
    public R<List<Object>> getRouters() {
        StpUtil.checkLogin();
        List<Map<String, Object>> routers = menuService.buildRouterVos(StpUtil.getLoginIdAsLong());
        return R.ok((List<Object>) (List<?>) routers);
    }
}
