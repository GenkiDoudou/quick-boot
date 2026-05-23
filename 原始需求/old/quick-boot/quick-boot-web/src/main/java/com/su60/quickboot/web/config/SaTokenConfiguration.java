package com.su60.quickboot.web.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpLogic;
import com.su60.quickboot.common.captcha.CaptchaService;
import com.su60.quickboot.common.captcha.CaptchaServiceImpl;
import com.su60.quickboot.common.security.PasswordEncoder;
import com.su60.quickboot.common.security.PasswordEncoderFactories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 *
 * @author luyanan
 * @since 2024/12/19
 */
@Slf4j
@Configuration
public class SaTokenConfiguration implements WebMvcConfigurer {

    /**
     * 注册Sa-Token拦截器
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }

    @Bean
    public CaptchaService captchaService(CacheManager cacheManager) {
        return new CaptchaServiceImpl(cacheManager);
    }

    /**
     * 密码编码器Bean
     *
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
//
	// Sa-Token 整合 jwt (Simple 简单模式)
	@Bean
	public StpLogic getStpLogicJwt() {
		return new StpLogicJwtForStateless();
	}
}
