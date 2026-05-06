package io.github.genkidoudou.web.auth;

import cn.dev33.satoken.stp.StpUtil;
import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.api.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 临时登录与用户会话占位接口，对接 {@code quick-ui} 既有契约（{@code /login}、{@code /getInfo} 等）。
 * <p>
 * 凭证写死在服务端，仅用于联调；接入真实用户体系后应替换为数据库校验并移除本控制器内的常量账号逻辑。
 */
@RestController
public class AuthController {

    /** 占位用户名（与前端默认表单一致时可省去修改前端）。 */
    private static final String STUB_USERNAME = "admin";

    /** 占位密码（明文仅用于开发演示）。 */
    private static final String STUB_PASSWORD = "admin";

    /**
     * 账号密码登录。
     *
     * @param username  登录名
     * @param password  密码
     * @param captchaId 滑块验证码 id；占位阶段可忽略，不参与校验
     * @return {@code data.access_token} 供前端存入 Storage/Cookie
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam(required = false) String captchaId) {
        if (!STUB_USERNAME.equals(username) || !STUB_PASSWORD.equals(password)) {
            return R.error(HttpCodes.UNAUTHORIZED, "用户名或密码错误");
        }
        StpUtil.login(1L);
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
        Map<String, Object> user = new LinkedHashMap<>(4);
        user.put("userId", 1L);
        user.put("userName", STUB_USERNAME);
        user.put("avatar", "");

        Map<String, Object> data = new LinkedHashMap<>(4);
        data.put("user", user);
        data.put("roles", List.of("admin"));
        data.put("permissions", List.of("*:*:*"));
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
     * 动态路由树（若依风格）；占位返回空列表，前端仅挂载常量路由（如首页）。
     *
     * @return 路由数组，可为空
     */
    @GetMapping("/getRouters")
    public R<List<Object>> getRouters() {
        StpUtil.checkLogin();
        return R.ok(List.of());
    }
}
