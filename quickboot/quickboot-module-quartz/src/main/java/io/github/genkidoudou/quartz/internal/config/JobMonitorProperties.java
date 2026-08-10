package io.github.genkidoudou.quartz.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 定时任务监控模块配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "qc.monitor.job")
public class JobMonitorProperties {

    /** 导出最大行数，默认 10000。 */
    private int exportMaxRows = 10000;
}
