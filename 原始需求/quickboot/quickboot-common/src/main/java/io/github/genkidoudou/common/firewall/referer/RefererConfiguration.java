package io.github.genkidoudou.common.firewall.referer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 请求来源拦截配置类
 * 
 * 自动配置请求来源拦截功能
 * 当配置 qc.security.firewall.referer.enabled=true 时启用
 *
 * @author QuickBoot
 * @since 2026/03/03
 */
@Configuration
@EnableConfigurationProperties(RefererProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.referer", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class RefererConfiguration {

    /**
     * 请求来源拦截配置属性
     *
     * @since 2026/03/03
     */
    private final RefererProperties properties;

    /**
     * JSON 对象映射器
     *
     * @since 2026/03/03
     */
    private final ObjectMapper objectMapper;

    /**
     * 配置请求来源拦截过滤器
     *
     * @return 过滤器注册 Bean
     * @since 2026/03/03
     */
    @Bean
    public FilterRegistrationBean<RefererFilter> refererFilterRegistration() {
        FilterRegistrationBean<RefererFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RefererFilter(properties, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setName("refererFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        
        return registration;
    }
}
