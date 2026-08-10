package io.github.genkidoudou.system.internal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 登录相关配置装配。
 */
@Configuration
@EnableConfigurationProperties(LoginProperties.class)
public class LoginConfiguration {
}
