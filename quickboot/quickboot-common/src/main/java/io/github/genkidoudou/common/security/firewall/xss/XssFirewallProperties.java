package io.github.genkidoudou.common.security.firewall.xss;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * XSS 启发式防火墙配置，绑定前缀 {@code qc.security.firewall.xss}。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "qc.security.firewall.xss")
public class XssFirewallProperties {

    /**
     * 是否启用；默认 {@code false}。
     */
    private boolean enabled = false;

    /**
     * Ant 风格忽略路径。
     */
    private List<String> ignoreUrls = new ArrayList<>();

    /**
     * 自定义正则（Java {@link java.util.regex.Pattern} 语法）；非法模式将导致启动失败（fail-fast）。
     */
    private List<String> customPatterns = new ArrayList<>();

    /**
     * i18n 未命中时的兜底文案。
     */
    private String forbiddenMessage;
}
