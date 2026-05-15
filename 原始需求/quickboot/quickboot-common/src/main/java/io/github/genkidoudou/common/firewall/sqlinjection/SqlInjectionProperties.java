package io.github.genkidoudou.common.firewall.sqlinjection;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL注入拦截配置属性
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Data
@ConfigurationProperties(prefix = "qc.security.firewall.sql-injection")
public class SqlInjectionProperties {

    /**
     * 是否启用SQL注入拦截
     *
     * @since 2026/03/05
     */
    private Boolean enabled = false;

    /**
     * 忽略拦截的URL列表
     * 支持 Ant 风格路径匹配
     *
     * @since 2026/03/05
     */
    private List<String> ignoreUrls = new ArrayList<>();

    /**
     * SQL关键字列表
     * 如果为空，使用默认关键字
     *
     * @since 2026/03/05
     */
    private List<String> keywords = new ArrayList<>();
}
