package io.github.genkidoudou.common.info;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 应用信息
 *
 * @author luyanan
 * @since 2026/3/1
 */
@ConfigurationProperties(value = "qc.appinfo")
@Data
public class AppInfoProperties {


    /**
     * 项目名称
     *
     * @since 2026/3/1
     */
    private String applicationName;


    /**
     * 项目名(中文名)
     *
     * @since 2026/3/1
     */

    private String applicationTitle;

    /**
     * 版本号
     *
     * @since 2026/3/1
     */

    private String version;

    /**
     * 版权
     *
     * @since 2026/3/1
     */

    private String copyright;

    /**
     * 扩展信息
     *
     * @since 2026/3/1
     */


    private Map<String, Object> data;
}
