package io.github.genkidoudou.common.security.firewall.headers;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全防火墙：HTTP 安全响应头配置，绑定前缀 {@code qc.security.firewall.headers}。
 * <p>
 * 排除路径使用 Ant 风格（与 {@link org.springframework.util.AntPathMatcher} 一致）。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "qc.security.firewall.headers")
public class FirewallHeadersProperties {

    /**
     * 是否启用安全头注入；为 {@code false} 时不注册 Filter（默认关闭）。
     */
    private boolean enabled = false;

    /**
     * 对应响应头 {@code X-Frame-Options}；空白时使用内建默认 {@code SAMEORIGIN}（见 Filter）。
     */
    private String frameOptions;

    /**
     * 对应 {@code X-Content-Type-Options}；空白时默认 {@code nosniff}。
     */
    private String contentTypeOptions;

    /**
     * 对应 {@code X-XSS-Protection}；空白时默认 {@code 1; mode=block}。
     */
    private String xssProtection;

    /**
     * 对应 {@code Content-Security-Policy}；仅非空时写入（强策略头）。
     */
    private String contentSecurityPolicy;

    /**
     * 对应 {@code Strict-Transport-Security}；仅非空时写入（强策略头）。
     */
    private String strictTransportSecurity;

    /**
     * 对应 {@code Referrer-Policy}；空白时默认 {@code strict-origin-when-cross-origin}。
     */
    private String referrerPolicy;

    /**
     * 对应 {@code Permissions-Policy}；仅非空时写入（强策略头）。
     */
    private String permissionsPolicy;

    /**
     * 命中则本模块不写入任一已管理安全头（完全跳过）。
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * 命中且未命中 {@link #excludeUrls} 时：只写基础四头，不写 CSP/HSTS/Permissions-Policy。
     */
    private List<String> excludeFromStrictPolicyUrls = new ArrayList<>();
}
