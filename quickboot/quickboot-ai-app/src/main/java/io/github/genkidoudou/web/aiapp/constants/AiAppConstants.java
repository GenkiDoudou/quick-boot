package io.github.genkidoudou.web.aiapp.constants;

/**
 * AI 应用模块通用常量。
 */
public final class AiAppConstants {

    /** 未逻辑删除。 */
    public static final int NOT_DELETED = 0;

    /** 已逻辑删除。 */
    public static final int DELETED = 1;

    /** 嵌入未启用。 */
    public static final int EMBED_DISABLED = 0;

    /** 嵌入已启用。 */
    public static final int EMBED_ENABLED = 1;

    /** 知识库检索 Tool 固定名称。 */
    public static final String TOOL_SEARCH_KNOWLEDGE = "search_knowledge";

    private AiAppConstants() {
    }
}
