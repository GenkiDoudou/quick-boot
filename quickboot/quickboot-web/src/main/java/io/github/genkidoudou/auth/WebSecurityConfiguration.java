package io.github.genkidoudou.auth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 {@link WebSecurityProperties}。
 */
@Configuration
@EnableConfigurationProperties(WebSecurityProperties.class)
public class WebSecurityConfiguration {
}
