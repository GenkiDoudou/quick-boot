package io.github.genkidoudou.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.plugins.secondary.SecondaryVerificationApplication;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapBuilder;
import io.github.genkidoudou.common.captcha.CaptchaProperties;
import io.github.genkidoudou.common.core.R;
import io.github.genkidoudou.common.exception.WarningException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 授权
 *
 * @author luyanan
 * @since 2026/3/12
 */

@RequiredArgsConstructor
@RestController
@Slf4j
public class AuthController {
    private final ImageCaptchaApplication application;

    /**
     * 登录
     *
     * @param username  用户名
     * @param password  密码
     * @param captchaId 验证码ID（tianai验证码通过后返回的ID）
     * @return
     * @since 2026/3/13
     */
    @SaIgnore
    @PostMapping("/login")
    public R login(@RequestParam(value = "username") String username,
                   @RequestParam(value = "password") String password,
                   @RequestParam(value = "captchaId", required = false) String captchaId) {

        // 验证码校验：captchaId 必须存在（由前端在验证成功后传入）
        if (captchaId == null || captchaId.isEmpty()) {
            return R.error("请先完成验证码验证");
        }

        boolean valid = ((SecondaryVerificationApplication) application).secondaryVerification(captchaId);
        if (!valid) {
            throw new WarningException(30602);
        }

        if (username.equals("admin") && password.equals("admin")) {
            StpUtil.login("admin");
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            String tokenValue = tokenInfo.getTokenValue();
            Map<String, Object> data = new HashMap<>();
            data.put("access_token", tokenValue);
            long tokenTimeout = tokenInfo.getTokenTimeout();
            data.put("expires_in", tokenTimeout);
            return R.ok(data);
        }
        return R.error("登录失败");
    }


    @GetMapping("getInfo")
    public R info() {
        Object loginId = StpUtil.getLoginId();

        Map<String, Object> build = MapBuilder.create(new HashMap<String, Object>())
                .put("user", MapBuilder.create().put("userId", 1)
                        .put("userName", "admin").build())
                .put("roles", new String[]{"admin"})
                .put("permissions", new ArrayList<>())
                .build();
        return R.ok(build);
    }

    @PostMapping("logout")
    public R logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        return R.ok();
    }

    @GetMapping("getRouters")
    public R getRouters() {

        return R.ok(new ArrayList<>());
    }

}
