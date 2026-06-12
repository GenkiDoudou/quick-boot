package io.github.genkidoudou.web.knowledge.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

/**
 * Spring AI 调用外部 LLM/Embedding 时的 HTTP 客户端修正。
 * <p>
 * {@code spring-ai-tika-document-reader} 会传递引入 Brotli（{@code br}）解压库，RestClient 若接受 {@code br} 编码，
 * 在解析 DeepSeek、百炼等 OpenAI 兼容响应时可能解压失败，表现为 JSON 仅读到 {@code \{} 即 EOF
 *（参见 <a href="https://github.com/spring-projects/spring-ai/issues/372">spring-ai#372</a>）。
 */
@Configuration
@ConditionalOnProperty(prefix = "qc.knowledge", name = "enabled", havingValue = "true")
public class KnowledgeAiHttpConfiguration {

    /**
     * 限制 Accept-Encoding 为 gzip/deflate，避免 Brotli 响应体解压异常。
     *
     * @return 供 Spring AI OpenAI 客户端使用的 RestClient 构建器
     */
    @Bean
    @Primary
    public RestClient.Builder knowledgeRestClientBuilder() {
        return RestClient.builder()
            .defaultHeaders(headers -> headers.set("Accept-Encoding", "gzip, deflate"));
    }
}
