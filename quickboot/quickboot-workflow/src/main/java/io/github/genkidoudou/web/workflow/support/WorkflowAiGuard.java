package io.github.genkidoudou.web.workflow.support;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.ai.registry.AiModelResolver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 工作流 AI 运行时可用性守卫。
 */
@Component
public class WorkflowAiGuard {

    private final ObjectProvider<AiModelResolver> aiModelResolver;
    private final ObjectProvider<ChatModel> chatModelProvider;

    public WorkflowAiGuard(ObjectProvider<AiModelResolver> aiModelResolver,
                           ObjectProvider<ChatModel> chatModelProvider) {
        this.aiModelResolver = aiModelResolver;
        this.chatModelProvider = chatModelProvider;
    }

    public void requireChatModel(Long workflowId) {
        requireChatModelInstance(workflowId);
    }

    public void requireChatModel() {
        requireChatModel(null);
    }

    /**
     * 获取工作流 ChatModel。
     *
     * @param workflowId 工作流 ID，可为 null
     * @return ChatModel
     */
    public ChatModel requireChatModelInstance(Long workflowId) {
        return requireChatModelInstance(workflowId, null);
    }

    /**
     * 获取工作流 ChatModel，支持节点级模型覆盖。
     *
     * @param workflowId   工作流 ID，可为 null
     * @param nodeModelId  节点指定模型 ID，可为 null
     * @return ChatModel
     */
    public ChatModel requireChatModelInstance(Long workflowId, Long nodeModelId) {
        AiModelResolver resolver = aiModelResolver.getIfAvailable();
        if (resolver != null) {
            return resolver.resolveWorkflowChat(workflowId, nodeModelId);
        }
        if (chatModelProvider.getIfAvailable() == null) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_AI_UNAVAILABLE,
                "Chat 模型未配置或不可用，请检查大模型管理或 spring.ai 配置");
        }
        return chatModelProvider.getObject();
    }
}
