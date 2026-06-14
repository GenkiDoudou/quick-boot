package io.github.genkidoudou.web.ai.prompt.support;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.ai.config.AiProperties;
import io.github.genkidoudou.web.ai.constants.AiDefaultSlot;
import io.github.genkidoudou.web.ai.mapper.AiModelMapper;
import io.github.genkidoudou.web.ai.registry.AiModelRegistry;
import io.github.genkidoudou.web.ai.registry.AiModelResolver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 提示词优化场景的 Chat 模型解析：入参 modelId → WORKFLOW_CHAT → CHAT 默认。
 */
@Component
public class AiPromptChatSupport {

    private final AiProperties aiProperties;
    private final ObjectProvider<AiModelResolver> modelResolverProvider;
    private final ObjectProvider<AiModelRegistry> modelRegistryProvider;
    private final ObjectProvider<AiModelMapper> modelMapperProvider;

    public AiPromptChatSupport(AiProperties aiProperties,
                               ObjectProvider<AiModelResolver> modelResolverProvider,
                               ObjectProvider<AiModelRegistry> modelRegistryProvider,
                               ObjectProvider<AiModelMapper> modelMapperProvider) {
        this.aiProperties = aiProperties;
        this.modelResolverProvider = modelResolverProvider;
        this.modelRegistryProvider = modelRegistryProvider;
        this.modelMapperProvider = modelMapperProvider;
    }

    /**
     * 按全局默认解析 Chat 模型。
     *
     * @param overrideModelId 请求指定的模型 ID，可为 null
     * @return ChatModel 与实际使用的 modelId
     */
    public ResolvedChatModel requireChatModel(Long overrideModelId) {
        requireAiEnabled();
        AiModelRegistry registry = modelRegistryProvider.getIfAvailable();
        AiModelMapper modelMapper = modelMapperProvider.getIfAvailable();
        if (registry != null && modelMapper != null) {
            for (Long modelId : buildCandidates(overrideModelId, modelMapper)) {
                if (modelId == null) {
                    continue;
                }
                ChatModel chatModel = registry.getChatModel(modelId);
                if (chatModel != null) {
                    return new ResolvedChatModel(modelId, chatModel);
                }
            }
        }
        AiModelResolver resolver = modelResolverProvider.getIfAvailable();
        if (resolver != null) {
            ChatModel chatModel = resolver.resolveWorkflowChat(null, overrideModelId);
            return new ResolvedChatModel(overrideModelId, chatModel);
        }
        throw aiUnavailable();
    }

    /**
     * 校验 AI 功能开关。
     */
    public void requireAiEnabled() {
        if (!aiProperties.isEnabled()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "AI 功能未启用（qc.ai.enabled=false），请开启后配置 Chat 模型");
        }
    }

    private Long[] buildCandidates(Long overrideModelId, AiModelMapper modelMapper) {
        return new Long[]{
            overrideModelId,
            modelMapper.selectDefaultModelIdBySlot(AiDefaultSlot.WORKFLOW_CHAT),
            modelMapper.selectDefaultModelIdBySlot(AiDefaultSlot.CHAT)
        };
    }

    private WarningException aiUnavailable() {
        return new WarningException(ErrorCodes.Common.INVALID_PARAM,
            "Chat 模型未配置，请在「大模型管理」中配置全局默认或检查 spring.ai YAML");
    }

    /**
     * 解析后的 Chat 模型与 ID。
     *
     * @param modelId   模型 ID
     * @param chatModel Chat 实例
     */
    public record ResolvedChatModel(Long modelId, ChatModel chatModel) {
    }
}
