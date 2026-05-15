package io.github.genkidoudou.common.firewall.methodandhost;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求方式和域名拦截配置属性
 * 
 * 支持配置允许的 HTTP 请求方式和访问域名
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
@Data
@ConfigurationProperties(prefix = "qc.security.firewall.method-and-host")
public class MethodAndHostProperties {

    /**
     * 是否启用请求方式和域名拦截
     * 默认：false
     *
     * @since 2026/03/02
     */
    private Boolean enabled = false;

    /**
     * 允许的 HTTP 请求方式列表
     * 支持：GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE
     * 示例：[GET, POST]
     *
     * @since 2026/03/02
     */
    private List<String> allowedMethods = new ArrayList<>();

    /**
     * 允许的访问域名列表
     * 支持精确匹配和通配符匹配
     * 示例：
     * - localhost:9100（精确匹配）
     * - example.com（精确匹配）
     * - *.example.com（通配符匹配子域名）
     * - localhost:*（任意端口）
     *
     * @since 2026/03/02
     */
    private List<String> allowedHosts = new ArrayList<>();

    /**
     * 排除的 URL 列表（不进行拦截）
     * 支持 Ant 风格路径匹配
     * 示例：/health, /actuator/**
     *
     * @since 2026/03/02
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * 拦截时的提示信息
     * 默认：请求被拒绝
     *
     * @since 2026/03/02
     */
    private String forbiddenMessage = "请求被拒绝";
}
