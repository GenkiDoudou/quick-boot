package io.github.genkidoudou.common.firewall.xss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * XSS 脚本注入拦截配置属性
 *
 * @author genkidoudou
 * @since 2026/03/06
 */
@Data
@ConfigurationProperties(prefix = "qc.security.firewall.xss")
public class XssProperties {

    /**
     * 是否启用 XSS 拦截
     * 默认：false
     *
     * @since 2026/03/06
     */
    private Boolean enabled = false;

    /**
     * 忽略拦截的 URL 列表
     * 支持 Ant 风格路径匹配
     *
     * @since 2026/03/06
     */
    private List<String> ignoreUrls = new ArrayList<>();

    /**
     * 自定义 XSS 检测模式（正则表达式）
     * 为空时使用默认模式
     *
     * @since 2026/03/06
     */
    private List<String> customPatterns = new ArrayList<>();
}
