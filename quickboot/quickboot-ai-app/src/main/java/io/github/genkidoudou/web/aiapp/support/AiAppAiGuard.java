package io.github.genkidoudou.web.aiapp.support;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.ai.registry.AiModelResolver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * AI 应用 Chat 模型运行时守卫。
 * <p>
 * 使用 {@link AiModelResolver#resolveWorkflowChat(Long, Long)} 解析应用绑定的 chatModelId。
 */
@Component
public class AiAppAiGuard {

    private final ObjectProvider<AiModelResolver> aiModelResolver;
    private final ObjectProvider<ChatModel> chatModelProvider;

    public AiAppAiGuard(ObjectProvider<AiModelResolver> aiModelResolver,
                        ObjectProvider<ChatModel> chatModelProvider) {
        this.aiModelResolver = aiModelResolver;
        this.chatModelProvider = chatModelProvider;
    }

    /**
     * 获取应用 Chat 模型实例。
     *
     * @param chatModelId 应用 config 中的 chatModelId
     * @return ChatModel
     */
    public ChatModel requireChatModel(Long chatModelId) {
        AiModelResolver resolver = aiModelResolver.getIfAvailable();
        if (resolver != null) {
            return resolver.resolveWorkflowChat(null, chatModelId);
        }
        if (chatModelProvider.getIfAvailable() == null) {
            throw new WarningException(ErrorCodes.Biz.KNOWLEDGE_AI_UNAVAILABLE,
                "Chat 模型未配置或不可用，请检查大模型管理或 spring.ai 配置");
        }
        return chatModelProvider.getObject();
    }
}
