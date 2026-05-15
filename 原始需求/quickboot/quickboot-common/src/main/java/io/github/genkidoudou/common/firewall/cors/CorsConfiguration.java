package io.github.genkidoudou.common.firewall.cors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;

/**
 * 跨域配置
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.cors", name = "enabled", havingValue = "true")
public class CorsConfiguration {

    private final CorsProperties corsProperties;

    /**
     * 配置跨域资源共享
     *
     * @return CORS配置源
     * @since 2026/03/05
     */
//    @Bean
//    public CorsFilter corsConfigurationSource() {
//        log.info("初始化跨域配置，允许的源：{}", corsProperties.getAllowedOrigins());
//
//        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
////
////        // 设置允许的源
////        if (corsProperties.getAllowedOrigins() != null && !corsProperties.getAllowedOrigins().isEmpty()) {
////            corsProperties.getAllowedOrigins().forEach(configuration::addAllowedOrigin);
////        }
////
////        // 设置允许的请求方法
////        if (corsProperties.getAllowedMethods() != null && !corsProperties.getAllowedMethods().isEmpty()) {
////            corsProperties.getAllowedMethods().forEach(configuration::addAllowedMethod);
////        }
////
////        // 设置允许的请求头
////        if (corsProperties.getAllowedHeaders() != null && !corsProperties.getAllowedHeaders().isEmpty()) {
////            corsProperties.getAllowedHeaders().forEach(configuration::addAllowedHeader);
////        }
////
////        // 设置暴露的响应头
////        if (corsProperties.getExposedHeaders() != null && !corsProperties.getExposedHeaders().isEmpty()) {
////            corsProperties.getExposedHeaders().forEach(configuration::addExposedHeader);
////        }
////
////        // 设置是否允许携带凭证
////        configuration.setAllowCredentials(corsProperties.getAllowCredentials());
////
////        // 设置预检请求的缓存时间
////        configuration.setMaxAge(corsProperties.getMaxAge());
////
////        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
////        source.registerCorsConfiguration(corsProperties.getPathPattern(), configuration);
//
//        // 允许的源（前端地址）
//        configuration.addAllowedOriginPattern("*");
//        // 开发环境可临时使用 "*"，但 credentials=true 时不能用 "*"
//        // config.addAllowedOriginPattern("*");
//
//        // 允许的请求方法
//        configuration.addAllowedMethod("*");
//
//        // 允许的请求头
//        configuration.addAllowedHeader("*");
//
//        // 是否允许携带凭证（cookie、authorization等）
//        configuration.setAllowCredentials(true);
//
//        // 预检请求缓存时间（秒）
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        // 对所有接口生效，或指定 "/api/**"
//        source.registerCorsConfiguration("/**", configuration);
//
//        return new CorsFilter(source);
//
//
//    }
    @Bean
    public FilterRegistrationBean coreWebFilter(CorsProperties corsProperties) {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        // * 号表示匹配任意的
        config.setAllowedMethods(Arrays.asList("*"));
        config.setAllowedOrigins(Arrays.asList("*"));
        config.setAllowedHeaders(Arrays.asList("*"));
        PathPatternParser patternParser = new PathPatternParser();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(patternParser);
        // ** 代表所有
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }

}
