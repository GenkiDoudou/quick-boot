package io.github.genkidoudou.common.firewall.cors;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨域配置属性
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Data
@ConfigurationProperties(prefix = "qc.security.firewall.cors")
public class CorsProperties {

    /**
     * 是否启用跨域配置
     *
     * @since 2026/03/05
     */
    private Boolean enabled = false;

    /**
     * 允许的源（域名）列表
     * 支持通配符 *
     *
     * @since 2026/03/05
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * 允许的请求方法列表
     *
     * @since 2026/03/05
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    /**
     * 允许的请求头列表
     *
     * @since 2026/03/05
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * 暴露的响应头列表
     *
     * @since 2026/03/05
     */
    private List<String> exposedHeaders = new ArrayList<>();

    /**
     * 是否允许携带凭证（Cookie）
     *
     * @since 2026/03/05
     */
    private Boolean allowCredentials = true;

    /**
     * 预检请求的缓存时间（秒）
     *
     * @since 2026/03/05
     */
    private Long maxAge = 3600L;

    /**
     * 跨域配置应用的路径模式
     *
     * @since 2026/03/05
     */
    private String pathPattern = "/**";
}
