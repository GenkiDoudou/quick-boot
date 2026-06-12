package io.github.genkidoudou.web.ai.constants;

/**
 * 全局默认模型槽位，对应 {@code ai_model.default_slot}。
 */
public final class AiDefaultSlot {

    /** 全局默认 Chat 模型。 */
    public static final String CHAT = "CHAT";

    /** 全局默认 Embedding 模型。 */
    public static final String EMBEDDING = "EMBEDDING";

    /** 全局默认工作流 Chat 模型。 */
    public static final String WORKFLOW_CHAT = "WORKFLOW_CHAT";

    private AiDefaultSlot() {
    }
}
