package io.github.genkidoudou.common.firewall.sensitiveword;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词过滤配置属性
 * 
 * 支持配置敏感词白名单、黑名单、忽略URL、过滤策略等
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
@Data
@ConfigurationProperties(prefix = "qc.security.firewall.sensitive-word")
public class SensitiveWordProperties {

    /**
     * 是否启用敏感词过滤
     * 默认：false
     *
     * @since 2026/03/02
     */
    private Boolean enable = false;

    /**
     * 敏感词白名单文件路径列表
     * 支持 classpath: 和 file: 前缀
     * 示例：classpath:sensitive-word-white.txt
     *
     * @since 2026/03/02
     */
    private List<String> whiteList = new ArrayList<>();

    /**
     * 敏感词黑名单文件路径列表
     * 支持 classpath: 和 file: 前缀
     * 示例：classpath:sensitive-word-black.txt
     *
     * @since 2026/03/02
     */
    private List<String> blackList = new ArrayList<>();

    /**
     * 忽略敏感词过滤的URL列表
     * 支持 Ant 风格路径匹配
     * 示例：/login, /api/public/**
     *
     * @since 2026/03/02
     */
    private List<String> ignoreUrls = new ArrayList<>();

    /**
     * 敏感词过滤策略
     * REPLACE：替换为 *
     * THROW：抛出异常
     * 默认：REPLACE
     *
     * @since 2026/03/02
     */
    private FilterStrategy strategy = FilterStrategy.REPLACE;

    /**
     * 敏感词过滤策略枚举
     *
     * @since 2026/03/02
     */
    public enum FilterStrategy {
        /**
         * 替换为 *
         */
        REPLACE,
        
        /**
         * 抛出异常
         */
        THROW
    }
}
