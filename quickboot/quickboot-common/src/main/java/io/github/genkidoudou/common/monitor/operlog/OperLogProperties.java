package io.github.genkidoudou.common.monitor.operlog;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志采集与导出相关配置。
 */
@Data
@ConfigurationProperties(prefix = "qc.monitor.operlog")
public class OperLogProperties {

    /** 是否启用宽切面采集。 */
    private boolean captureEnabled = true;

    /**
     * 是否在控制台打印请求摘要（参照旧栈 WebPrintLoggerEventListener）。
     */
    private boolean print = false;

    /**
     * 是否异步写入 {@code sys_oper_log}（默认 true，避免同步落库拖慢接口）。
     */
    private boolean asyncEnabled = true;

    /** 同步导出最大行数。 */
    private int exportMaxRows = 10_000;

    /**
     * 匹配请求 URI 前缀时跳过发布事件（Ant 风格，如 {@code /actuator/**}）。
     * 默认覆盖登录、文档、监控端点等。
     */
    private List<String> ignoreUrlPatterns = defaultIgnorePatterns();

    private static List<String> defaultIgnorePatterns() {
        List<String> p = new ArrayList<>();
        p.add("/login/captcha-config");
        p.add("/logout");
        p.add("/actuator/**");
        p.add("/swagger-ui/**");
        p.add("/v3/api-docs/**");
        p.add("/v3/api-docs");
        p.add("/webjars/**");
        p.add("/error");
        p.add("/doc.html");
        p.add("/swagger-resources/**");
        p.add("/favicon.ico");
        p.add("/monitor/operlog/**");
        return p;
    }
}
