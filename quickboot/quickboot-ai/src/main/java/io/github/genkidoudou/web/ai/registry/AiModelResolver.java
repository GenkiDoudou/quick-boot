package io.github.genkidoudou.web.ai.registry;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.ai.config.AiProperties;
import io.github.genkidoudou.web.ai.constants.AiDefaultSlot;
import io.github.genkidoudou.web.ai.mapper.AiModelMapper;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 运行时模型解析：KB/WF 绑定 → 全局 default_slot → YAML Bean 回落。
 */
@Component
public class AiModelResolver {

    private final AiModelMapper modelMapper;
    private final AiModelRegistry modelRegistry;
    private final AiProperties aiProperties;
    private final ObjectProvider<ChatModel> yamlChatModelProvider;
    private final ObjectProvider<EmbeddingModel> yamlEmbeddingModelProvider;

    public AiModelResolver(AiModelMapper modelMapper,
                           AiModelRegistry modelRegistry,
                           AiProperties aiProperties,
                           ObjectProvider<ChatModel> yamlChatModelProvider,
                           ObjectProvider<EmbeddingModel> yamlEmbeddingModelProvider) {
        this.modelMapper = modelMapper;
        this.modelRegistry = modelRegistry;
        this.aiProperties = aiProperties;
        this.yamlChatModelProvider = yamlChatModelProvider;
        this.yamlEmbeddingModelProvider = yamlEmbeddingModelProvider;
    }

    /**
     * 解析知识库 Chat 模型。
     * <p>
     * 优先级：kb.chat_model_id → default_slot=CHAT → YAML ChatModel Bean。
     *
     * @param kbId 知识库 ID，可为 null（仅查全局默认）
     * @return ChatModel
     */
    public ChatModel resolveChat(Long kbId) {
        Long modelId = kbId == null ? null : modelMapper.selectChatModelIdByKbId(kbId);
        ChatModel model = resolveChatByModelId(modelId);
        if (model != null) {
            return model;
        }
        modelId = modelMapper.selectDefaultModelIdBySlot(AiDefaultSlot.CHAT);
        model = resolveChatByModelId(modelId);
        if (model != null) {
            return model;
        }
        return fallbackYamlChat();
    }

    /**
     * 解析知识库 Embedding 模型。
     * <p>
     * 优先级：kb.embedding_model_id → default_slot=EMBEDDING → YAML EmbeddingModel Bean。
     *
     * @param kbId 知识库 ID，可为 null
     * @return EmbeddingModel
     */
    public EmbeddingModel resolveEmbedding(Long kbId) {
        Long modelId = kbId == null ? null : modelMapper.selectEmbeddingModelIdByKbId(kbId);
        EmbeddingModel model = resolveEmbeddingByModelId(modelId);
        if (model != null) {
            return model;
        }
        modelId = modelMapper.selectDefaultModelIdBySlot(AiDefaultSlot.EMBEDDING);
        model = resolveEmbeddingByModelId(modelId);
        if (model != null) {
            return model;
        }
        return fallbackYamlEmbedding();
    }

    /**
     * 解析工作流 Chat 模型。
     * <p>
     * 优先级：wf.chat_model_id → WORKFLOW_CHAT 默认 → CHAT 默认 → YAML Bean。
     *
     * @param workflowId 工作流 ID
     * @return ChatModel
     */
    public ChatModel resolveWorkflowChat(Long workflowId) {
        Long modelId = workflowId == null ? null : modelMapper.selectChatModelIdByWorkflowId(workflowId);
        ChatModel model = resolveChatByModelId(modelId);
        if (model != null) {
            return model;
        }
        modelId = modelMapper.selectDefaultModelIdBySlot(AiDefaultSlot.WORKFLOW_CHAT);
        model = resolveChatByModelId(modelId);
        if (model != null) {
            return model;
        }
        modelId = modelMapper.selectDefaultModelIdBySlot(AiDefaultSlot.CHAT);
        model = resolveChatByModelId(modelId);
        if (model != null) {
            return model;
        }
        return fallbackYamlChat();
    }

    private ChatModel resolveChatByModelId(Long modelId) {
        if (modelId == null) {
            return null;
        }
        return modelRegistry.getChatModel(modelId);
    }

    private EmbeddingModel resolveEmbeddingByModelId(Long modelId) {
        if (modelId == null) {
            return null;
        }
        return modelRegistry.getEmbeddingModel(modelId);
    }

    private ChatModel fallbackYamlChat() {
        if (!aiProperties.getRegistry().isFallbackToYaml()) {
            throw missingModel("Chat");
        }
        ChatModel model = yamlChatModelProvider.getIfAvailable();
        if (model == null) {
            throw missingModel("Chat");
        }
        return model;
    }

    private EmbeddingModel fallbackYamlEmbedding() {
        if (!aiProperties.getRegistry().isFallbackToYaml()) {
            throw missingModel("Embedding");
        }
        EmbeddingModel model = yamlEmbeddingModelProvider.getIfAvailable();
        if (model == null) {
            throw missingModel("Embedding");
        }
        return model;
    }

    private WarningException missingModel(String type) {
        return new WarningException(ErrorCodes.Common.INVALID_PARAM,
            type + " 模型未配置，请在「大模型管理」中配置全局默认或检查 spring.ai YAML");
    }
}
