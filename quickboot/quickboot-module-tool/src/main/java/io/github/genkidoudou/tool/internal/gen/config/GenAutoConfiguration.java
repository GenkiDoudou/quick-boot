package io.github.genkidoudou.tool.internal.gen.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 代码生成模块自动配置。
 */
@Configuration
@EnableConfigurationProperties(GenProperties.class)
public class GenAutoConfiguration {
}
