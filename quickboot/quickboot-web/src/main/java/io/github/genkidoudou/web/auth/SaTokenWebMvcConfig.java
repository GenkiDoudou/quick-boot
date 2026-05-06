package io.github.genkidoudou.web.auth;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 登录校验拦截：除匿名接口外需携带有效 {@code Authorization: Bearer}。
 */
@Configuration
public class SaTokenWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/logout",
                        "/phoneLogin",
                        "/sendSms",
                        "/qrcodeLogin",
                        "/captchaImage",
                        "/qrcodeImage",
                        "/actuator/**",
                        "/error",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/h2-console",
                        "/h2-console/**"
                );
    }
}
