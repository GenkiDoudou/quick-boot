package io.github.genkidoudou.web.aiapp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * AI 应用模块自动配置入口。
 * <p>
 * 当 {@code qc.ai-app.enabled=true} 时注册 Controller、Service、Mapper 及运行时相关 Bean。
 */
@Configuration
@ConditionalOnProperty(prefix = "qc.ai-app", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AiAppProperties.class)
@ComponentScan("io.github.genkidoudou.web.aiapp")
public class AiAppAutoConfiguration {
}
