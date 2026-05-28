package io.github.genkidoudou.web.tool.gen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 代码生成模块配置。
 */
@Data
@ConfigurationProperties(prefix = "quickboot.gen")
public class GenProperties {

    /** 生成注释作者。 */
    private String author = "quickboot";

    /** Java 根包名。 */
    private String packageName = "io.github.genkidoudou.web";

    /** 默认模块名。 */
    private String moduleName = "system";

    /** Zip 下载默认文件名。 */
    private String zipFileName = "quickboot.zip";

    /** 单次建表允许的最大语句条数。 */
    private int createTableMaxStatements = 10;
}
