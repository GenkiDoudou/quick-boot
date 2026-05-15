package io.github.genkidoudou.common.firewall.referer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求来源拦截配置属性
 * 
 * 支持配置允许的 Referer 列表
 *
 * @author QuickBoot
 * @since 2026/03/03
 */
@Data
@ConfigurationProperties(prefix = "qc.security.firewall.referer")
public class RefererProperties {

    /**
     * 是否启用请求来源拦截
     * 默认：false
     *
     * @since 2026/03/03
     */
    private Boolean enabled = false;

    /**
     * 允许的 Referer 列表
     * 支持完整 URL 和通配符匹配
     * 示例：
     * - http://localhost:8089/
     * - https://example.com/
     * - https://*.example.com/（通配符匹配子域名）
     *
     * @since 2026/03/03
     */
    private List<String> allowedReferers = new ArrayList<>();

    /**
     * 排除的 URL 列表（不进行拦截）
     * 支持 Ant 风格路径匹配
     * 示例：/health, /actuator/**
     *
     * @since 2026/03/03
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * 是否允许空 Referer
     * true: 允许直接访问（Referer 为空）
     * false: 拒绝直接访问
     * 默认：false
     *
     * @since 2026/03/03
     */
    private Boolean allowEmptyReferer = false;

    /**
     * 拦截时的提示信息
     * 默认：请求来源不合法
     *
     * @since 2026/03/03
     */
    private String forbiddenMessage = "请求来源不合法";
}
