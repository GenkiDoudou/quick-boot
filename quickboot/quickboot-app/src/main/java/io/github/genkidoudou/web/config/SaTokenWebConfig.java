package io.github.genkidoudou.web.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import io.github.genkidoudou.report.internal.config.JimuProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * sa-token：登录拦截 + 注解鉴权；超级权限 {@code *:*:*} 视为拥有全部权限字符。
 */
@Configuration
public class SaTokenWebConfig implements WebMvcConfigurer {

  private final ObjectProvider<JimuProperties> jimuProperties;

  @Value("${qc.jimu.enabled:true}")
  private boolean jimuEnabled;

  public SaTokenWebConfig(ObjectProvider<JimuProperties> jimuProperties) {
    this.jimuProperties = jimuProperties;
  }

    /** 重写 sa-token 权限匹配：{@code *:*:*} 或 {@code *} 视为拥有全部权限字符。 */
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

  /**
   * 注册登录拦截器；积木路径按 {@link JimuProperties} 排除 sa-token 校验。
   * 副作用：未登录访问非排除路径将抛出 NotLoginException。
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    List<String> excludes = new ArrayList<>(List.of(
      "/",
      "/login",
      "/error",
      "/favicon.ico",
      "/h2-console/**",
      "/actuator/**",
      "/api/captcha/**",
      "/file/preview/**"
    ));
    if (jimuEnabled) {
      JimuProperties props = jimuProperties.getIfAvailable();
      List<String> jimuPaths = props != null && props.getSecurity() != null
        ? props.getSecurity().getExcludeSaTokenPaths()
        : null;
      if (CollectionUtils.isEmpty(jimuPaths)) {
        excludes.addAll(List.of("/jmreport/**", "/drag/**", "/jimubi/**", "/jimureport/**"));
      } else {
        excludes.addAll(jimuPaths);
      }
    }
    registry.addInterceptor(new SaInterceptor(handle -> SaRouter.match("/**")
        .notMatch(excludes.toArray(String[]::new))
        .check(r -> StpUtil.checkLogin())))
      .addPathPatterns("/**");
  }
}
