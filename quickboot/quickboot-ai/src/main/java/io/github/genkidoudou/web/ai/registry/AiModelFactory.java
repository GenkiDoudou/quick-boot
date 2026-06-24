package io.github.genkidoudou.web.ai.registry;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.ai.constants.AiModelType;
import io.github.genkidoudou.web.ai.constants.AiProvider;
import io.github.genkidoudou.web.ai.domain.AiModel;
import io.github.genkidoudou.web.ai.support.AiSecretSupport;
import io.github.genkidoudou.web.ai.support.DeepSeekThinkingDisableInterceptor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * 按 DB 配置程序化构建 Spring AI {@link ChatModel} / {@link EmbeddingModel} 实例。
 */
@Component
public class AiModelFactory {

    private final PasswordCodec passwordCodec;

    public AiModelFactory(PasswordCodec passwordCodec) {
        this.passwordCodec = passwordCodec;
    }

    /**
     * 构建 Chat 模型实例。
     *
     * @param config 库表配置
     * @return ChatModel
     */
    public ChatModel buildChatModel(AiModel config) {
        if (!AiModelType.isLanguage(config.getModelType())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "模型类型不匹配，期望语言模型，实际 " + config.getModelType());
        }
        int timeoutMs = resolveTimeout(config);
        String apiKey = resolveApiKey(config);
        return switch (config.getProvider()) {
            case AiProvider.OLLAMA -> buildOllamaChatModel(config, timeoutMs);
            default -> {
                if (AiProvider.isOpenAiCompatible(config.getProvider())) {
                    yield buildOpenAiChatModel(config, apiKey, timeoutMs);
                }
                throw invalidProvider(config.getProvider());
            }
        };
    }

    /**
     * 构建 Embedding 模型实例。
     *
     * @param config 库表配置
     * @return EmbeddingModel
     */
    public EmbeddingModel buildEmbeddingModel(AiModel config) {
        if (!AiModelType.isVector(config.getModelType())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "模型类型不匹配，期望向量模型，实际 " + config.getModelType());
        }
        int timeoutMs = resolveTimeout(config);
        String apiKey = resolveApiKey(config);
        return switch (config.getProvider()) {
            case AiProvider.OLLAMA -> buildOllamaEmbeddingModel(config, timeoutMs);
            default -> {
                if (AiProvider.isOpenAiCompatible(config.getProvider())) {
                    yield buildOpenAiEmbeddingModel(config, apiKey, timeoutMs);
                }
                throw invalidProvider(config.getProvider());
            }
        };
    }

    private ChatModel buildOpenAiChatModel(AiModel config, String apiKey, int timeoutMs) {
        RestClient.Builder restBuilder = restClientBuilder(timeoutMs);
        if (AiProvider.DEEPSEEK.equals(config.getProvider())) {
            restBuilder = restBuilder.requestInterceptor(new DeepSeekThinkingDisableInterceptor());
        }
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
            .baseUrl(config.getBaseUrl())
            .restClientBuilder(restBuilder);
        if (StrUtil.isNotBlank(apiKey)) {
            apiBuilder.apiKey(apiKey);
        }
        if (StrUtil.isNotBlank(config.getCompletionsPath())) {
            apiBuilder.completionsPath(config.getCompletionsPath());
        } else if (AiProvider.DEEPSEEK.equals(config.getProvider())) {
            apiBuilder.completionsPath("/chat/completions");
        }
        if (StrUtil.isNotBlank(config.getEmbeddingsPath())) {
            apiBuilder.embeddingsPath(config.getEmbeddingsPath());
        }
        OpenAiApi openAiApi = apiBuilder.build();

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
            .model(config.getModelName());
        if (config.getTemperature() != null) {
            optionsBuilder.temperature(config.getTemperature().doubleValue());
        }
        if (config.getMaxTokens() != null) {
            optionsBuilder.maxTokens(config.getMaxTokens());
        }
        return OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(optionsBuilder.build())
            .build();
    }

    private EmbeddingModel buildOpenAiEmbeddingModel(AiModel config, String apiKey, int timeoutMs) {
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
            .baseUrl(config.getBaseUrl())
            .restClientBuilder(restClientBuilder(timeoutMs));
        if (StrUtil.isNotBlank(apiKey)) {
            apiBuilder.apiKey(apiKey);
        }
        if (StrUtil.isNotBlank(config.getEmbeddingsPath())) {
            apiBuilder.embeddingsPath(config.getEmbeddingsPath());
        }
        OpenAiApi openAiApi = apiBuilder.build();

        OpenAiEmbeddingOptions.Builder optionsBuilder = OpenAiEmbeddingOptions.builder()
            .model(config.getModelName());
        if (config.getDimensions() != null) {
            optionsBuilder.dimensions(config.getDimensions());
        }
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, optionsBuilder.build());
    }

    private ChatModel buildOllamaChatModel(AiModel config, int timeoutMs) {
        OllamaApi ollamaApi = OllamaApi.builder()
            .baseUrl(config.getBaseUrl())
            .restClientBuilder(restClientBuilder(timeoutMs))
            .build();
        OllamaOptions.Builder optionsBuilder = OllamaOptions.builder()
            .model(config.getModelName());
        if (config.getTemperature() != null) {
            optionsBuilder.temperature(config.getTemperature().doubleValue());
        }
        return OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(optionsBuilder.build())
            .build();
    }

    private EmbeddingModel buildOllamaEmbeddingModel(AiModel config, int timeoutMs) {
        OllamaApi ollamaApi = OllamaApi.builder()
            .baseUrl(config.getBaseUrl())
            .restClientBuilder(restClientBuilder(timeoutMs))
            .build();
        OllamaOptions options = OllamaOptions.builder()
            .model(config.getModelName())
            .build();
        return OllamaEmbeddingModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(options)
            .build();
    }

    private RestClient.Builder restClientBuilder(int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder().requestFactory(factory);
    }

    private String resolveApiKey(AiModel config) {
        if (AiProvider.isOllama(config.getProvider()) && StrUtil.isBlank(config.getApiKey())) {
            return "";
        }
        String plain = AiSecretSupport.resolvePlainValue(passwordCodec, config.getApiKeyType(), config.getApiKey());
        if (plain == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "环境变量未设置: " + config.getApiKey());
        }
        return plain;
    }

    private int resolveTimeout(AiModel config) {
        return config.getRequestTimeoutMs() == null ? 60_000 : config.getRequestTimeoutMs();
    }

    private WarningException invalidProvider(String provider) {
        return new WarningException(ErrorCodes.Common.INVALID_PARAM, "不支持的 Provider: " + provider);
    }
}
