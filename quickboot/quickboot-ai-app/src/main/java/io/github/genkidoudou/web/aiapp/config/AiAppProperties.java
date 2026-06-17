package io.github.genkidoudou.web.aiapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 应用模块配置（{@code qc.ai-app.*}）。
 */
@Data
@ConfigurationProperties(prefix = "qc.ai-app")
public class AiAppProperties {

    /** 是否启用 AI 应用模块；为 false 时不注册相关 Bean。 */
    private boolean enabled = false;

    /** 单轮对话 Tool 调用上限。 */
    private int maxToolCalls = 5;

    /** 聊天 SSE 超时（毫秒）。 */
    private long chatTimeoutMs = 120_000L;
}
