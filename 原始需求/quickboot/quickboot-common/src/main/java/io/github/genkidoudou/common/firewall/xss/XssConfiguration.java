package io.github.genkidoudou.common.firewall.xss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * XSS 脚本注入拦截配置类
 *
 * @author genkidoudou
 * @since 2026/03/06
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(XssProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.xss", name = "enabled", havingValue = "true")
public class XssConfiguration {

    private final XssProperties properties;

    /**
     * 注册 XSS 过滤器
     *
     * @return 过滤器注册 Bean
     * @since 2026/03/06
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        log.info("初始化 XSS 脚本注入拦截，忽略 URL: {}", properties.getIgnoreUrls());

        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter(properties));
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 4);

        return registration;
    }
}
