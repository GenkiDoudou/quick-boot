package com.su60.quickboot.data.spring;

import com.su60.quickboot.data.spring.restful.RestFulAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * spring扩展自动配置类
 *
 * @author luyanan
 * @since 2023/09/10
 **/
@Configuration
@ConditionalOnProperty(prefix = SpringExtendProperties.PREFIX, value = "enable", matchIfMissing = false, havingValue = "true")
@Import({RestFulAutoConfiguration.class, SpringContextHolder.class, WebConfig.class})
@EnableConfigurationProperties({SpringExtendProperties.class})
public class SpringExtendAutoConfiguration {


}
