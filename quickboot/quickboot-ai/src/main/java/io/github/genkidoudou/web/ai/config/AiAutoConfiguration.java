package io.github.genkidoudou.web.ai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * AI 大模型模块自动配置入口。
 * <p>
 * 当 {@code qc.ai.enabled=true} 时注册 Controller、Service、Mapper 及 Registry 相关 Bean。
 */
@Configuration
@ConditionalOnProperty(prefix = "qc.ai", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AiProperties.class)
@ComponentScan("io.github.genkidoudou.web.ai")
public class AiAutoConfiguration {
}
