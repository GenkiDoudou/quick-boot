package io.github.genkidoudou.common.firewall.headers;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全头配置属性
 * <p>
 * 用于配置 HTTP 响应中的安全相关头，防止点击劫持、XSS 等攻击
 *
 * @author genkidoudou
 * @since 2026/03/06
 */
@Data
@ConfigurationProperties(prefix = "qc.security.firewall.headers")
public class SecurityHeaderProperties {

    /**
     * 是否启用安全头
     * 默认：true
     *
     * @since 2026/03/06
     */
    private Boolean enabled = false;

    /**
     * X-Frame-Options 配置
     * <p>
     * 防止点击劫持，可选值：
     * <ul>
     *   <li>DENY - 禁止任何页面嵌入</li>
     *   <li>SAMEORIGIN - 仅允许同源页面嵌入</li>
     * </ul>
     * 设为空或 null 则不添加该头
     *
     * @since 2026/03/06
     */
    private String frameOptions = "SAMEORIGIN";

    /**
     * X-Content-Type-Options 配置
     * <p>
     * 防止 MIME 类型嗅探，通常设为 nosniff
     * 设为空或 null 则不添加该头
     *
     * @since 2026/03/06
     */
    private String contentTypeOptions = "nosniff";

    /**
     * X-XSS-Protection 配置
     * <p>
     * 启用浏览器 XSS 过滤器，可选值：0 | 1 | 1; mode=block
     * 设为空或 null 则不添加该头
     * 注意：该头已被现代浏览器弃用，推荐使用 Content-Security-Policy
     *
     * @since 2026/03/06
     */
    private String xssProtection = "1; mode=block";

    /**
     * Content-Security-Policy 配置
     * <p>
     * 内容安全策略，控制资源加载来源
     * 设为空或 null 则不添加该头
     *
     * @since 2026/03/06
     */
    private String contentSecurityPolicy;

    /**
     * Strict-Transport-Security (HSTS) 配置
     * <p>
     * 强制使用 HTTPS，格式如：max-age=31536000; includeSubDomains
     * 设为空或 null 则不添加该头（仅 HTTPS 站点建议启用）
     *
     * @since 2026/03/06
     */
    private String strictTransportSecurity;

    /**
     * Referrer-Policy 配置
     * <p>
     * 控制 Referer 头的发送，可选值：no-referrer | no-referrer-when-downgrade | origin 等
     * 设为空或 null 则不添加该头
     *
     * @since 2026/03/06
     */
    private String referrerPolicy = "strict-origin-when-cross-origin";

    /**
     * Permissions-Policy 配置
     * <p>
     * 控制浏览器功能权限，如：geolocation=(), microphone=()
     * 设为空或 null 则不添加该头
     *
     * @since 2026/03/06
     */
    private String permissionsPolicy;

    /**
     * 排除的 URL 列表（不添加安全头）
     * 支持 Ant 风格路径匹配
     *
     * @since 2026/03/06
     */
    private List<String> excludeUrls = new ArrayList<>();
}
