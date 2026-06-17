package io.github.genkidoudou.web.aiapp.constants;

/**
 * AI 应用类型常量，对应 {@code ai_app.app_type}。
 */
public final class AiAppType {

    /** 智能体模式：Tool Calling + 变量记忆。 */
    public static final String AGENT = "agent";

    /** 高级编排模式：绑定单个工作流。 */
    public static final String WORKFLOW = "workflow";

    private AiAppType() {
    }
}
