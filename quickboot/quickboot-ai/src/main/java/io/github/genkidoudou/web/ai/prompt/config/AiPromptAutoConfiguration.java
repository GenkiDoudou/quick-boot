package io.github.genkidoudou.web.ai.prompt.config;

import io.github.genkidoudou.web.ai.config.AiProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 提示词管理模块自动配置（无条件注册，CRUD 不依赖 {@code qc.ai.enabled}）。
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
@ComponentScan("io.github.genkidoudou.web.ai.prompt")
@MapperScan("io.github.genkidoudou.web.ai.prompt.mapper")
public class AiPromptAutoConfiguration {
}
