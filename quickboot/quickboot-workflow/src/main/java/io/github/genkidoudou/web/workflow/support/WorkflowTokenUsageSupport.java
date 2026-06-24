package io.github.genkidoudou.web.workflow.support;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 从 Spring AI 响应中提取 Token 用量，供步骤 Trace 展示。
 */
public final class WorkflowTokenUsageSupport {

    public record CallTextAndUsage(String text, Map<String, Object> tokenUsage) {
    }

    private WorkflowTokenUsageSupport() {
    }

    /**
     * 解析 {@link ChatClient.CallResponseSpec}：仅触发一次 advisor 链，避免重复调用
     * {@code content()} / {@code chatResponse()} 导致 No CallAdvisors available。
     *
     * @param response ChatClient 调用响应
     * @return 文本与 Token 用量
     */
    public static CallTextAndUsage resolveCall(ChatClient.CallResponseSpec response) {
        if (response == null) {
            return new CallTextAndUsage("", null);
        }
        ChatResponse chatResponse = response.chatResponse();
        return new CallTextAndUsage(extractText(chatResponse), fromChatResponse(chatResponse));
    }

    /**
     * 从 ChatResponse 提取 Token 用量。
     *
     * @param response 模型响应
     * @return prompt/completion/total tokens；无数据时返回 null
     */
    public static Map<String, Object> fromChatResponse(ChatResponse response) {
        if (response == null || response.getMetadata() == null) {
            return null;
        }
        Usage usage = response.getMetadata().getUsage();
        if (usage == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        if (usage.getPromptTokens() != null) {
            map.put("promptTokens", usage.getPromptTokens());
        }
        if (usage.getCompletionTokens() != null) {
            map.put("completionTokens", usage.getCompletionTokens());
        }
        if (usage.getTotalTokens() != null) {
            map.put("totalTokens", usage.getTotalTokens());
        } else if (!map.isEmpty()) {
            long prompt = usage.getPromptTokens() == null ? 0L : usage.getPromptTokens();
            long completion = usage.getCompletionTokens() == null ? 0L : usage.getCompletionTokens();
            if (prompt + completion > 0) {
                map.put("totalTokens", prompt + completion);
            }
        }
        return map.isEmpty() ? null : map;
    }

    private static String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text;
    }
}
