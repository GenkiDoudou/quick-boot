package io.github.genkidoudou.common.exporttask;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 导出编排配置自动装配。
 */
@Configuration
@EnableConfigurationProperties(QcExportProperties.class)
public class ExportAutoConfiguration {
}
