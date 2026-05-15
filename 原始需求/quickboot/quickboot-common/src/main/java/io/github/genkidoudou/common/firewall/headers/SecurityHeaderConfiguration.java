package io.github.genkidoudou.common.firewall.headers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 安全头配置类
 * <p>
 * 当配置 qc.security.firewall.headers.enabled=true 时启用（默认启用）
 * 通过 SecurityHeaderFilter 为响应添加安全头
 *
 * @author genkidoudou
 * @since 2026/03/06
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityHeaderProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.headers", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SecurityHeaderConfiguration {

    private final SecurityHeaderProperties properties;

    /**
     * 注册安全头过滤器
     *
     * @return 过滤器注册 Bean
     * @since 2026/03/06
     */
    @Bean
    public FilterRegistrationBean<SecurityHeaderFilter> securityHeaderFilterRegistration() {
        log.info("初始化安全头配置，frame-options={}", properties.getFrameOptions());

        FilterRegistrationBean<SecurityHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeaderFilter(properties));
        registration.addUrlPatterns("/*");
        registration.setName("securityHeaderFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE - 1); // 尽早执行，确保所有响应都有安全头

        return registration;
    }
}
