package io.github.genkidoudou.tool.internal.gen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 代码生成模块配置。
 */
@Data
@ConfigurationProperties(prefix = "qc.gen")
public class GenProperties {

    /** 生成注释作者。 */
    private String author = "quickboot";

    /** Java 模块根包名（生成物落在 {@code .internal.*}）。 */
    private String packageName = "io.github.genkidoudou.system";

    /** 默认模块名（前端路径 / 权限前缀）。 */
    private String moduleName = "system";

    /** Zip 下载默认文件名。 */
    private String zipFileName = "quickboot.zip";

    /** 单次建表允许的最大语句条数。 */
    private int createTableMaxStatements = 10;
}
