package io.github.genkidoudou.common.security.firewall.sqlinjection;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL 注入启发式防火墙配置，绑定前缀 {@code qc.security.firewall.sql-injection}。
 * <p>
 * {@code keywords} 非空时仅使用配置列表；为空时使用内置默认关键字集合。
 * </p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "qc.security.firewall.sql-injection")
public class SqlInjectionFirewallProperties {

    /**
     * 是否启用；默认 {@code false}，为 {@code true} 时注册 Filter。
     */
    private boolean enabled = false;

    /**
     * Ant 风格忽略路径（相对应用内路径，与 {@link org.springframework.util.AntPathMatcher} 一致）；命中则跳过本能力。
     */
    private List<String> ignoreUrls = new ArrayList<>();

    /**
     * JSON body 中跳过 SQL 关键字扫描的字段名（精确匹配属性名）。
     * <p>
     * 例如 {@code apiPathPatterns} 含 Ant 路径 {@code /**}，会误命中内置关键字 {@code /*}。
     * </p>
     */
    private List<String> ignoreJsonFields = new ArrayList<>(List.of("apiPathPatterns"));

    /**
     * 自定义关键字；非空时<strong>仅</strong>使用本列表，否则使用内置默认关键字。
     */
    private List<String> keywords = new ArrayList<>();

    /**
     * i18n 词条（{@code String.valueOf(code)}）未命中时的兜底文案。
     */
    private String forbiddenMessage;
}
