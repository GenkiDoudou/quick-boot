package com.su60.quickboot.data.spring.restful;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 统一响应返回自动配置类
 *
 * @author luyanan
 * @since 2023/09/10
 **/
@ConditionalOnProperty(prefix = RestFulProperties.PREFIX, value = "enable", havingValue = "true", matchIfMissing = false)
@Configuration
@EnableConfigurationProperties(RestFulProperties.class)
public class RestFulAutoConfiguration {

    /**
     * 统一响应返回包装
     *
     * @param restFulProperties 配置类
     * @return RestFulResultHandler
     * @since 2023/09/10
     */
    @Bean
    public RestFulResultHandler restFulResultHandler(RestFulProperties restFulProperties) {
        return new RestFulResultHandler(restFulProperties);
    }

}
