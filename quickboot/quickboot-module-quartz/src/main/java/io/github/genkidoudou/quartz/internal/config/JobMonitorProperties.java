package io.github.genkidoudou.quartz.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 定时任务监控模块配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "qc.monitor.job")
public class JobMonitorProperties {

    /** 导出最大行数，默认 10000。 */
    private int exportMaxRows = 10000;

    /** HTTP 任务相关配置。 */
    private Http http = new Http();

    /** 本地脚本任务相关配置。 */
    private Script script = new Script();

    @Data
    public static class Http {
        /** 是否允许 HTTP 类型任务，默认 true。 */
        private boolean enabled = true;
        /** 是否拦截内网/本机地址（SSRF 防护），默认 true。 */
        private boolean blockPrivateNetwork = true;
        /** 允许的主机白名单；为空表示不限制公网主机（仍受 blockPrivateNetwork 约束）。 */
        private List<String> allowedHosts = new ArrayList<>();
    }

    @Data
    public static class Script {
        /** 是否允许脚本类型任务，默认 false。 */
        private boolean enabled = false;
        /** 脚本必须位于以下目录之一（绝对路径）。 */
        private List<String> allowedDirs = new ArrayList<>();
    }
}
