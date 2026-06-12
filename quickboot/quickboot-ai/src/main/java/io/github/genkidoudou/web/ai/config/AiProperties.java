package io.github.genkidoudou.web.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 大模型管理配置（{@code qc.ai.*}）。
 */
@Data
@ConfigurationProperties(prefix = "qc.ai")
public class AiProperties {

    /** 是否启用 AI 模型管理与 Registry；为 false 时不注册 Controller 与运行时 Bean。 */
    private boolean enabled = false;

    /**
     * Embedding 维度期望值，须与 {@code qc.knowledge.vectorDimensions} 保持一致。
     */
    private int vectorDimensions = 768;

    /** Registry 运行时配置。 */
    private Registry registry = new Registry();

    /** 导出相关配置。 */
    private Export export = new Export();

    /**
     * 模型实例缓存与解析回落策略。
     */
    @Data
    public static class Registry {

        /** DB 无可用默认时是否回落 YAML 自动配置的 ChatModel / EmbeddingModel Bean。 */
        private boolean fallbackToYaml = true;

        /** 模型客户端缓存 TTL（秒）。 */
        private int clientCacheTtlSeconds = 300;

        /** 连接测试超时（毫秒）。 */
        private int testTimeoutMs = 20_000;
    }

    @Data
    public static class Export {

        /** 默认导出是否包含明文密钥。 */
        private boolean includeSecrets = false;
    }
}
