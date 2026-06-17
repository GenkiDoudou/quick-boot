package io.github.genkidoudou.web.aiapp.constants;

/**
 * AI 应用消息角色常量，对应 {@code ai_app_message.role}。
 */
public final class AiAppMessageRole {

    /** 用户消息。 */
    public static final String USER = "user";

    /** 助手回复。 */
    public static final String ASSISTANT = "assistant";

    /** 工具调用结果。 */
    public static final String TOOL = "tool";

    private AiAppMessageRole() {
    }
}
