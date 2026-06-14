package io.github.genkidoudou.web.ai.prompt.constants;

/**
 * AI 优化会话状态，对应 {@code ai_prompt_optimize_session.status}。
 */
public final class AiPromptOptimizeStatus {

    /** 优化成功且 JSON 解析通过。 */
    public static final String SUCCESS = "SUCCESS";

    /** 优化失败或 JSON 解析失败。 */
    public static final String FAILED = "FAILED";

    private AiPromptOptimizeStatus() {
    }
}
