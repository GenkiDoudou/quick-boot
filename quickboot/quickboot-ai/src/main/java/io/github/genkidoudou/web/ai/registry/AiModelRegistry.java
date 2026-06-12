package io.github.genkidoudou.web.ai.registry;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.web.ai.config.AiProperties;
import io.github.genkidoudou.web.ai.constants.AiConstants;
import io.github.genkidoudou.web.ai.constants.AiModelType;
import io.github.genkidoudou.web.ai.domain.AiModel;
import io.github.genkidoudou.web.ai.mapper.AiModelMapper;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * AI 模型实例缓存：按 {@code model_id} 复用 ChatModel / EmbeddingModel，支持 TTL 与手动驱逐。
 */
@Component
public class AiModelRegistry {

    private final AiModelMapper modelMapper;
    private final AiModelFactory modelFactory;
    private final AiProperties aiProperties;

    private final Cache<Long, ChatModel> chatCache;
    private final Cache<Long, EmbeddingModel> embeddingCache;

    public AiModelRegistry(AiModelMapper modelMapper,
                           AiModelFactory modelFactory,
                           AiProperties aiProperties) {
        this.modelMapper = modelMapper;
        this.modelFactory = modelFactory;
        this.aiProperties = aiProperties;
        int ttl = Math.max(0, aiProperties.getRegistry().getClientCacheTtlSeconds());
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        if (ttl > 0) {
            builder.expireAfterWrite(ttl, TimeUnit.SECONDS);
        }
        this.chatCache = builder.build();
        this.embeddingCache = builder.build();
    }

    /**
     * 获取 Chat 模型实例（缓存 miss 时读库构建）。
     *
     * @param modelId 模型主键
     * @return ChatModel；不存在或停用时返回 null
     */
    public ChatModel getChatModel(Long modelId) {
        if (modelId == null) {
            return null;
        }
        ChatModel cached = chatCache.getIfPresent(modelId);
        if (cached != null) {
            return cached;
        }
        AiModel row = loadActiveModel(modelId, AiModelType.CHAT);
        if (row == null) {
            return null;
        }
        ChatModel model = modelFactory.buildChatModel(row);
        chatCache.put(modelId, model);
        return model;
    }

    /**
     * 获取 Embedding 模型实例（缓存 miss 时读库构建）。
     *
     * @param modelId 模型主键
     * @return EmbeddingModel；不存在或停用时返回 null
     */
    public EmbeddingModel getEmbeddingModel(Long modelId) {
        if (modelId == null) {
            return null;
        }
        EmbeddingModel cached = embeddingCache.getIfPresent(modelId);
        if (cached != null) {
            return cached;
        }
        AiModel row = loadActiveModel(modelId, AiModelType.EMBEDDING);
        if (row == null) {
            return null;
        }
        EmbeddingModel model = modelFactory.buildEmbeddingModel(row);
        embeddingCache.put(modelId, model);
        return model;
    }

    /**
     * 驱逐指定模型的缓存实例。
     *
     * @param modelId 模型主键
     */
    public void evict(Long modelId) {
        if (modelId == null) {
            return;
        }
        chatCache.invalidate(modelId);
        embeddingCache.invalidate(modelId);
    }

    /**
     * 驱逐全部缓存。
     */
    public void evictAll() {
        chatCache.invalidateAll();
        embeddingCache.invalidateAll();
    }

    /**
     * 加载启用中的模型配置。
     *
     * @param modelId   模型主键
     * @param modelType 期望类型
     * @return 实体；不可用返回 null
     */
    public AiModel loadActiveModel(Long modelId, String modelType) {
        AiModel row = modelMapper.selectById(modelId);
        if (row == null || AiConstants.DELETED == row.getDeleted()) {
            return null;
        }
        if (AiConstants.STATUS_DISABLED == row.getStatus()) {
            return null;
        }
        if (modelType != null) {
            if (AiModelType.CHAT.equals(modelType) && !AiModelType.isLanguage(row.getModelType())) {
                return null;
            }
            if (AiModelType.EMBEDDING.equals(modelType) && !AiModelType.isVector(row.getModelType())) {
                return null;
            }
        }
        return row;
    }

    /**
     * 按 ID 加载模型（含停用项，供管理端使用）。
     *
     * @param modelId 模型主键
     * @return 实体；不存在或已删除时返回 null
     */
    public AiModel loadModel(Long modelId) {
        if (modelId == null) {
            return null;
        }
        AiModel row = modelMapper.selectById(modelId);
        if (row == null || AiConstants.DELETED == row.getDeleted()) {
            return null;
        }
        return row;
    }

    /**
     * 清除同 default_slot 的旧默认标记（设默认前调用）。
     *
     * @param defaultSlot 槽位
     */
    public void clearDefaultSlot(String defaultSlot) {
        modelMapper.update(null, Wrappers.<AiModel>lambdaUpdate()
            .eq(AiModel::getDefaultSlot, defaultSlot)
            .eq(AiModel::getDeleted, AiConstants.NOT_DELETED)
            .set(AiModel::getDefaultSlot, null));
    }

    /**
     * 获取 Registry 配置（供 ConnectionTester 读取超时等）。
     *
     * @return AiProperties
     */
    public AiProperties getAiProperties() {
        return aiProperties;
    }
}
