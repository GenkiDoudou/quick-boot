package io.github.genkidoudou.common.firewall.client;

import io.github.genkidoudou.common.firewall.client.impl.ConfigClientServiceImpl;
import io.github.genkidoudou.common.firewall.password.DelegatingPasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 客户端管理自动配置
 *
 * @author luyanan
 * @since 2026-03-04
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ClientProperties.class)
@ConditionalOnProperty(
        prefix = "qc.security.firewall.client",
        name = "enabled",
        havingValue = "true"
)
public class ClientConfiguration {

    @ConditionalOnProperty(
            prefix = "qc.security.firewall.client",
            name = "source",
            havingValue = "config",
            matchIfMissing = true  // 默认使用配置文件
    )
    @Bean
    public ClientService configClientService(ClientProperties clientProperties, DelegatingPasswordEncoder delegatingPasswordEncoder) {
        return new ConfigClientServiceImpl(clientProperties, delegatingPasswordEncoder);
    }

    /**
     * 注册客户端认证过滤器
     */
    @Bean
    public FilterRegistrationBean<ClientAuthFilter> clientAuthFilterRegistration(
            ClientService clientService,
            ClientProperties clientProperties
           ) {

        log.info("注册客户端认证过滤器");

        FilterRegistrationBean<ClientAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ClientAuthFilter(clientService, clientProperties));
        registration.addUrlPatterns("/*");
        registration.setName("clientAuthFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);

        return registration;
    }
}
