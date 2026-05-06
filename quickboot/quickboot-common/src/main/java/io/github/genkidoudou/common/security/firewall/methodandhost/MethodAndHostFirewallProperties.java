package io.github.genkidoudou.common.security.firewall.methodandhost;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全防火墙：请求方式与 Host 白名单拦截配置，绑定前缀 {@code qc.security.firewall.method-and-host}。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "qc.security.firewall.method-and-host")
public class MethodAndHostFirewallProperties {

    /**
     * 默认关闭；显式 {@code true} 时注册 Filter。
     */
    private boolean enabled = false;

    /**
     * 允许的 HTTP Method 白名单（如 GET、POST）；空列表表示放行所有方法。
     */
    private List<String> allowedMethods = new ArrayList<>();

    /**
     * 允许的 Host 白名单；空列表表示放行所有 Host。
     * <p>
     * 支持精确匹配（{@code example.com:8080}）、子域名通配（{@code *.example.com:*}）与端口通配（{@code localhost:*}）。
     */
    private List<String> allowedHosts = new ArrayList<>();

    /**
     * Ant 风格排除路径；命中则完全跳过本能力。
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * i18n 词条未命中时的兜底文案（不覆盖已命中的 i18n 文案）。
     */
    private String forbiddenMessage;
}

