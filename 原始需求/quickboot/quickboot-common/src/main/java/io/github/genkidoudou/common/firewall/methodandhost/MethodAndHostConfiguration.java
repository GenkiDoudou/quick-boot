package io.github.genkidoudou.common.firewall.methodandhost;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 请求方式和域名拦截配置类
 * 
 * 自动配置请求方式和域名拦截功能
 * 当配置 qc.security.firewall.method-and-host.enabled=true 时启用
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
@Configuration
@EnableConfigurationProperties(MethodAndHostProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.method-and-host", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class MethodAndHostConfiguration {

    /**
     * 请求方式和域名拦截配置属性
     *
     * @since 2026/03/02
     */
    private final MethodAndHostProperties properties;

    /**
     * JSON 对象映射器
     *
     * @since 2026/03/02
     */
    private final ObjectMapper objectMapper;

    /**
     * 配置请求方式和域名拦截过滤器
     *
     * @return 过滤器注册 Bean
     * @since 2026/03/02
     */
    @Bean
    public FilterRegistrationBean<MethodAndHostFilter> methodAndHostFilterRegistration() {
        FilterRegistrationBean<MethodAndHostFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MethodAndHostFilter(properties, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setName("methodAndHostFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        
        return registration;
    }
}
