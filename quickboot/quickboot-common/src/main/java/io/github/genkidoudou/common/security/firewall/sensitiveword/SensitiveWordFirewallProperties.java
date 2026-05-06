package io.github.genkidoudou.common.security.firewall.sensitiveword;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词防火墙配置，绑定前缀 {@code qc.security.firewall.sensitive-word}。
 * <p>
 * <b>注意（BREAKING）</b>：主开关键名为 {@code enabled}，与 {@code qc.security.firewall.headers.enabled} 一致；
 * 请勿再使用旧键 {@code enable}。
 * </p>
 * <p>YAML 示例：</p>
 * <pre>
 * qc:
 *   security:
 *     firewall:
 *       sensitive-word:
 *         enabled: true
 *         strategy: THROW
 *         ignore-urls:
 *           - /login
 *           - /captcha/**
 *         white-list:
 *           - classpath:sensitive-word-white.txt
 *         black-list:
 *           - classpath:sensitive-word-black.txt
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "qc.security.firewall.sensitive-word")
public class SensitiveWordFirewallProperties {

    /**
     * 是否启用敏感词 Filter；{@code false}（默认）时不注册 Filter。
     */
    private boolean enabled = false;

    /**
     * 白名单资源路径（{@code classpath:} / {@code file:}），每文件忽略空行与 {@code #} 注释行。
     */
    private List<String> whiteList = new ArrayList<>();

    /**
     * 黑名单资源路径；在内置默认词库基础上追加词表。
     */
    private List<String> blackList = new ArrayList<>();

    /**
     * 完全跳过本能力的 Ant 风格路径（相对 context-path 之后的 servlet path 语义与 Spring
     * {@link org.springframework.util.AntPathMatcher} 一致）。
     */
    private List<String> ignoreUrls = new ArrayList<>();

    /**
     * {@link SensitiveWordFirewallStrategy#name()}，如 {@code REPLACE}、{@code THROW}；YAML 中可用小写。
     */
    private SensitiveWordFirewallStrategy strategy = SensitiveWordFirewallStrategy.THROW;
}
