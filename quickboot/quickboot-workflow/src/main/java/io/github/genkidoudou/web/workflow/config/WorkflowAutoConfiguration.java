package io.github.genkidoudou.web.workflow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 工作流模块自动配置入口。
 * <p>
 * 当 {@code qc.workflow.enabled=true} 时注册 Controller、Service、Mapper 及引擎相关 Bean。
 */
@Configuration
@ConditionalOnProperty(prefix = "qc.workflow", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(WorkflowProperties.class)
@ComponentScan("io.github.genkidoudou.web.workflow")
public class WorkflowAutoConfiguration {
}
