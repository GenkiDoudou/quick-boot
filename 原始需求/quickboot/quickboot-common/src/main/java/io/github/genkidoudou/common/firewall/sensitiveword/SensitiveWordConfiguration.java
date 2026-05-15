package io.github.genkidoudou.common.firewall.sensitiveword;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;

/**
 * 敏感词过滤配置类
 * 
 * 自动配置敏感词过滤功能
 * 当配置 qc.security.firewall.sensitive-word.enable=true 时启用
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
@Configuration
@EnableConfigurationProperties(SensitiveWordProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.sensitive-word", name = "enable", havingValue = "true")
@RequiredArgsConstructor
public class SensitiveWordConfiguration {

    /**
     * 敏感词配置属性
     *
     * @since 2026/03/02
     */
    private final SensitiveWordProperties properties;

    /**
     * 资源加载器
     *
     * @since 2026/03/02
     */
    private final ResourceLoader resourceLoader;

    /**
     * JSON 对象映射器
     *
     * @since 2026/03/02
     */
    private final ObjectMapper objectMapper;

    /**
     * 配置敏感词服务
     *
     * @return 敏感词服务
     * @since 2026/03/02
     */
    @Bean
    public SensitiveWordService sensitiveWordService() {
        return new SensitiveWordService(properties, resourceLoader);
    }

    /**
     * 配置敏感词过滤器
     *
     * @param sensitiveWordService 敏感词服务
     * @return 过滤器注册 Bean
     * @since 2026/03/02
     */
    @Bean
    public FilterRegistrationBean<SensitiveWordFilter> sensitiveWordFilterRegistration(
            SensitiveWordService sensitiveWordService) {
        
        FilterRegistrationBean<SensitiveWordFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SensitiveWordFilter(sensitiveWordService, properties, objectMapper));
        registration.addUrlPatterns("/*");
        registration.setName("sensitiveWordFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        
        return registration;
    }
}
