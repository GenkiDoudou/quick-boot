package io.github.genkidoudou.web.knowledge.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 管理子模块自动配置：在知识库模块启用且 {@code qc.knowledge.mcp.enabled=true} 时注册 MCP Bean。
 */
@Configuration
@ConditionalOnProperty(prefix = "qc.knowledge.mcp", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(KnowledgeMcpProperties.class)
@ComponentScan("io.github.genkidoudou.web.knowledge.mcp")
public class KnowledgeMcpAutoConfiguration {
}
