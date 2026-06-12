package io.github.genkidoudou.web.knowledge.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 知识库模块自动配置入口。
 * <p>
 * 当 {@code qc.knowledge.enabled=true} 时注册 Controller、Service、Mapper 及 AI 相关 Bean。
 */
@Configuration
@ConditionalOnProperty(prefix = "qc.knowledge", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(KnowledgeProperties.class)
@ComponentScan("io.github.genkidoudou.web.knowledge")
public class KnowledgeAutoConfiguration {
}
