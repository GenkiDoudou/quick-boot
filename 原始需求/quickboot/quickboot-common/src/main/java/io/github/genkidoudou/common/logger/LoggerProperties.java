package io.github.genkidoudou.common.logger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志配置属性
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Data
@ConfigurationProperties(prefix = "qc.logger")
public class LoggerProperties {

    /**
     * 是否启用日志记录
     *
     * @since 2026/03/05
     */
    private Boolean enabled = true;

    /**
     * 是否打印日志到控制台
     *
     * @since 2026/03/05
     */
    private Boolean print = true;

    /**
     * 忽略日志记录的URL列表
     * 支持 Ant 风格路径匹配
     *
     * @since 2026/03/05
     */
    private List<String> ignoreUrls = new ArrayList<>();
}
