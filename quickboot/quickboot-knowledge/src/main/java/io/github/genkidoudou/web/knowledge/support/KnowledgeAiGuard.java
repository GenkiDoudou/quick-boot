package io.github.genkidoudou.web.knowledge.support;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.ai.registry.AiModelResolver;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 知识库 AI 运行时可用性守卫。
 * <p>
 * 优先经 {@link AiModelResolver} 解析 DB 配置模型，不可用时回落 YAML Bean。
 */
@Component
public class KnowledgeAiGuard {

    private final ObjectProvider<AiModelResolver> aiModelResolver;
    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final ObjectProvider<ChatModel> chatModelProvider;
    private final KnowledgeProperties properties;

    public KnowledgeAiGuard(ObjectProvider<AiModelResolver> aiModelResolver,
                            ObjectProvider<EmbeddingModel> embeddingModelProvider,
                            ObjectProvider<ChatModel> chatModelProvider,
                            KnowledgeProperties properties) {
        this.aiModelResolver = aiModelResolver;
        this.embeddingModelProvider = embeddingModelProvider;
        this.chatModelProvider = chatModelProvider;
        this.properties = properties;
    }

    /**
     * 要求 Embedding 模型可用。
     *
     * @param kbId 知识库 ID，可为 null
     */
    public void requireEmbeddingModel(Long kbId) {
        requireEmbeddingModelInstance(kbId);
    }

    /**
     * 兼容旧调用：使用全局解析链。
     */
    public void requireEmbeddingModel() {
        requireEmbeddingModel(null);
    }

    /**
     * 要求 Chat 模型可用。
     *
     * @param kbId 知识库 ID，可为 null
     */
    public void requireChatModel(Long kbId) {
        requireChatModelInstance(kbId);
    }

    public void requireChatModel() {
        requireChatModel(null);
    }

    public void requireRagModels(Long kbId) {
        requireEmbeddingModel(kbId);
        requireChatModel(kbId);
    }

    public void requireRagModels() {
        requireRagModels(null);
    }

    /**
     * 获取 ChatModel 实例。
     *
     * @param kbId 知识库 ID
     * @return ChatModel
     */
    public ChatModel requireChatModelInstance(Long kbId) {
        AiModelResolver resolver = aiModelResolver.getIfAvailable();
        if (resolver != null) {
            return resolver.resolveChat(kbId);
        }
        if (chatModelProvider.getIfAvailable() == null) {
            throw unavailable("Chat 模型未配置或不可用，请检查大模型管理或 spring.ai 配置");
        }
        return chatModelProvider.getObject();
    }

    /**
     * 获取 EmbeddingModel 实例。
     *
     * @param kbId 知识库 ID
     * @return EmbeddingModel
     */
    public EmbeddingModel requireEmbeddingModelInstance(Long kbId) {
        AiModelResolver resolver = aiModelResolver.getIfAvailable();
        if (resolver != null) {
            return resolver.resolveEmbedding(kbId);
        }
        if (embeddingModelProvider.getIfAvailable() == null) {
            throw unavailable("Embedding 模型未配置或不可用，请检查大模型管理或 spring.ai 配置");
        }
        return embeddingModelProvider.getObject();
    }

    public boolean isOllamaRequired() {
        return properties.isOllamaRequired();
    }

    private WarningException unavailable(String message) {
        return new WarningException(ErrorCodes.Biz.KNOWLEDGE_AI_UNAVAILABLE, message);
    }
}
