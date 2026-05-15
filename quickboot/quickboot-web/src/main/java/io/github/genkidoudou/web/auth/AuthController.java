package io.github.genkidoudou.web.auth;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.plugins.secondary.SecondaryVerificationApplication;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.system.menu.service.MenuService;
import io.github.genkidoudou.web.system.user.datascope.DataScopeSession;
import io.github.genkidoudou.web.system.user.datascope.DataScopeSessionStore;
import io.github.genkidoudou.web.system.user.datascope.LoginDataScopeService;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
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
    private final SysUserMapper userMapper;

    private final ObjectProvider<ImageCaptchaApplication> imageCaptchaApplicationProvider;

    /** 是否校验登录行为验证码（关闭后 /login 不再校验 captchaId，便于自动化）。 */
    @Value("${qc.login.captcha-enabled:true}")
    private boolean loginCaptchaEnabled;

    /**
     * 账号密码登录。
     *
     * @param username  登录名
     * @param password  密码
     * @param captchaId 天爱验证码二次校验 id（{@code /api/captcha/validate} 成功后返回）
     * @return {@code data.access_token} 供前端存入 Storage/Cookie
     */


    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam(required = false) String captchaId) {
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
        long userId = authLoginService.authenticate(username, password);
        StpUtil.login(userId);
        loginDataScopeService.refreshSession(userId);
        Map<String, Object> data = new HashMap<>(2);
        data.put("access_token", StpUtil.getTokenValue());
        return R.ok(data);
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
        data.put("roles", menuService.listRoleKeysByUserId(userId));
        data.put("permissions", menuService.listPermissionsByUserId(userId));
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
