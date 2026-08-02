package io.github.genkidoudou.web.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * sa-token：登录拦截 + 注解鉴权；超级权限 {@code *:*:*} 视为拥有全部权限字符。
 */
@Configuration
public class SaTokenWebConfig implements WebMvcConfigurer {

  @PostConstruct
  public void rewriteSaStrategy() {
    SaStrategy.instance.hasElement = (list, element) -> {
      if (list == null || list.isEmpty() || element == null) {
        return false;
      }
      if (list.contains("*:*:*") || list.contains("*")) {
        return true;
      }
      return list.contains(element);
    };
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 注册拦截器：打开注解鉴权，并对业务路径校验登录
    registry.addInterceptor(new SaInterceptor(handle -> SaRouter.match("/**")
        .notMatch(
          "/",
          "/login",
          "/error",
          "/favicon.ico",
          "/h2-console/**",
          "/actuator/**",
          "/api/captcha/**"
        )
        .check(r -> StpUtil.checkLogin())))
      .addPathPatterns("/**");
  }
}
