package io.github.genkidoudou.common.security.firewall.cors;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code qc.security.firewall.cors.enabled=true} 时注册 CORS Filter。
 * <p>移植自 bak {@code FirewallCorsAutoConfiguration}。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(FirewallCorsProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.cors", name = "enabled", havingValue = "true")
public class FirewallCorsAutoConfiguration {

  /** 尽量早于可能写出拦截响应的 Filter，确保跨域响应也带 CORS 头。 */
  private static final int ORDER = Ordered.HIGHEST_PRECEDENCE;

  /**
   * 注册 CORS Filter，映射到 {@link FirewallCorsProperties#getPathPattern()}。
   *
   * @param properties CORS 配置
   * @return Filter 注册 Bean
   */
  @Bean
  public FilterRegistrationBean<CorsFilter> firewallCorsFilterRegistration(FirewallCorsProperties properties) {
    CorsConfiguration cors = buildCorsConfiguration(properties);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration(properties.getPathPattern(), cors);

    FilterRegistrationBean<CorsFilter> reg = new FilterRegistrationBean<>(new CorsFilter(source));
    reg.setOrder(ORDER);
    reg.addUrlPatterns("/*");
    return reg;
  }

  static CorsConfiguration buildCorsConfiguration(FirewallCorsProperties properties) {
    CorsConfiguration cors = new CorsConfiguration();
    cors.setAllowCredentials(properties.isAllowCredentials());
    cors.setMaxAge(properties.getMaxAge());
    cors.setAllowedMethods(copyOrNull(properties.getAllowedMethods()));
    cors.setAllowedHeaders(copyOrNull(properties.getAllowedHeaders()));

    if (!CollectionUtils.isEmpty(properties.getExposedHeaders())) {
      cors.setExposedHeaders(copyOrNull(properties.getExposedHeaders()));
    }

    List<String> origins = normalizeOrigins(properties.getAllowedOrigins());
    boolean containsWildcard = origins.stream().anyMatch("*"::equals);
    if (properties.isAllowCredentials() && containsWildcard) {
      // 规范限制：credentials=true 时不允许 ACAO="*"
      cors.setAllowedOriginPatterns(List.of("*"));
    }
    else {
      // 含端口通配（如 http://localhost:*）时用 patterns，对齐 bak ResourceServerConfig
      boolean hasPattern = origins.stream().anyMatch(o -> o.contains("*"));
      if (hasPattern) {
        cors.setAllowedOriginPatterns(origins);
      }
      else {
        cors.setAllowedOrigins(origins);
      }
    }
    return cors;
  }

  private static List<String> normalizeOrigins(List<String> configured) {
    if (CollectionUtils.isEmpty(configured)) {
      return List.of("*");
    }
    List<String> out = new ArrayList<>();
    for (String s : configured) {
      if (s == null) {
        continue;
      }
      String t = s.trim();
      if (!t.isEmpty()) {
        out.add(t);
      }
    }
    return out.isEmpty() ? List.of("*") : out;
  }

  private static List<String> copyOrNull(List<String> src) {
    if (src == null) {
      return null;
    }
    return new ArrayList<>(src);
  }
}
