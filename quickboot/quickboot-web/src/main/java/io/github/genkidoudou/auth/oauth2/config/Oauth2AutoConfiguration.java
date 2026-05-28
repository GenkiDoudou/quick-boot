package io.github.genkidoudou.auth.oauth2.config;

import io.github.genkidoudou.common.oauth2.Oauth2Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth2 配置属性注册。
 */
@Configuration
@EnableConfigurationProperties(Oauth2Properties.class)
public class Oauth2AutoConfiguration {
}
