package io.github.genkidoudou.auth;



import cn.dev33.satoken.interceptor.SaInterceptor;

import cn.dev33.satoken.stp.StpUtil;

import io.github.genkidoudou.report.config.JimuProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;

import org.springframework.util.CollectionUtils;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;



import java.util.ArrayList;

import java.util.List;



/**

 * Sa-Token 登录校验拦截：除匿名接口外需携带有效 {@code Authorization: Bearer}。

 * <p>

 * Actuator、H2 控制台等敏感路径不再默认匿名；由 {@link WebSecurityProperties#getAnonymousPaths()} 按环境配置。

 */

@Configuration

@RequiredArgsConstructor

public class SaTokenWebMvcConfig implements WebMvcConfigurer {



    private final WebSecurityProperties webSecurityProperties;

    private final ObjectProvider<JimuProperties> jimuPropertiesProvider;



    @Override

    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))

                .addPathPatterns("/**")

                .excludePathPatterns(buildExcludePaths());

    }



    private List<String> buildExcludePaths() {

        List<String> paths = new ArrayList<>();

        // --- 登录与会话（无需 Bearer） ---
        paths.add("/login");

        paths.add("/login/captcha-config");

        paths.add("/logout");

        // 预留：手机/短信/扫码登录（当前无 Controller 实现）
        paths.add("/phoneLogin");

        paths.add("/sendSms");

        paths.add("/qrcodeLogin");

        paths.add("/api/captcha/**");

        paths.add("/qrcodeImage");

        // --- OAuth2 授权服务器 / 开放 API / 第三方登录入口 ---
        paths.add("/oauth2/**");

        paths.add("/open-api/**");

        paths.add("/oauth/login/providers");

        // --- 框架与文档 ---
        paths.add("/error");

        paths.add("/swagger-ui.html");

        paths.add("/swagger-ui/**");

        paths.add("/v3/api-docs/**");

        JimuProperties jimu = jimuPropertiesProvider.getIfAvailable();
        if (jimu != null && jimu.isEnabled() && !CollectionUtils.isEmpty(jimu.getSecurity().getExcludeSaTokenPaths())) {
            paths.addAll(jimu.getSecurity().getExcludeSaTokenPaths());
        }

        // --- 环境配置额外匿名路径（application-*.yml → qc.web.security.anonymous-paths） ---
        if (!CollectionUtils.isEmpty(webSecurityProperties.getAnonymousPaths())) {

            paths.addAll(webSecurityProperties.getAnonymousPaths());

        }

        return paths;

    }

}


