package io.github.genkidoudou.web.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 工作流模块配置（{@code qc.workflow.*}）。
 */
@Data
@ConfigurationProperties(prefix = "qc.workflow")
public class WorkflowProperties {

    /** 是否启用工作流模块；为 false 时不注册相关 Bean。 */
    private boolean enabled = false;

    /** 对外 API 相关配置（P0 预留）。 */
    private ExternalApi externalApi = new ExternalApi();

    /** 同步 Debug 运行超时（毫秒）。 */
    private long syncDebugTimeoutMs = 60_000L;

    /** 异步运行超时（毫秒）。 */
    private long asyncTimeoutMs = 600_000L;

    /** 单用户最大并发运行数。 */
    private int maxConcurrentRunsPerUser = 3;

    /** SSE 流式相关配置。 */
    private Stream stream = new Stream();

    /** HTTP 请求节点配置。 */
    private HttpRequest httpRequest = new HttpRequest();

    /**
     * 对外 REST API 预留开关。
     */
    @Data
    public static class ExternalApi {

        /** P0 默认 false，不开放 API Key 调用。 */
        private boolean enabled = false;
    }

    /**
     * SSE 流式推送配置。
     */
    @Data
    public static class Stream {

        /** 心跳间隔（毫秒）。 */
        private long heartbeatIntervalMs = 15_000L;
    }

    /**
     * HTTP 请求节点安全与超时限制。
     */
    @Data
    public static class HttpRequest {

        /** 是否允许 HTTP 请求节点。 */
        private boolean enabled = true;

        /** 连接与读取超时（毫秒）。 */
        private int timeoutMs = 15_000;

        /** 响应体最大字节数。 */
        private int maxBytes = 5_242_880;

        /** 最大跟随重定向次数。 */
        private int maxRedirects = 3;

        /** 请求 User-Agent。 */
        private String userAgent = "QuickBoot-WorkflowBot/1.0";
    }
}
