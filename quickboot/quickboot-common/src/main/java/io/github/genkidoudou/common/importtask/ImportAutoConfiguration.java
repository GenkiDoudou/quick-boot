package io.github.genkidoudou.common.importtask;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 导入编排配置自动装配。
 */
@AutoConfiguration
@EnableConfigurationProperties(QcImportProperties.class)
public class ImportAutoConfiguration {
}
