package io.github.genkidoudou.web.ai.prompt.constants;

/**
 * 提示词模块通用常量。
 */
public final class AiPromptConstants {

    /** 提示词正文在 content 表中的 section_key。 */
    public static final String CONTENT_SECTION_KEY = "content";

    /** 草稿内容/变量使用的 version_id 占位值。 */
    public static final Long DRAFT_VERSION_ID = 0L;

    /** AI 优化与 A/B 调用超时（毫秒）。 */
    public static final long OPTIMIZE_TIMEOUT_MS = 60_000L;

    /** 逻辑删除：未删除。 */
    public static final int NOT_DELETED = 0;

    /** 逻辑删除：已删除。 */
    public static final int DELETED = 1;

    private AiPromptConstants() {
    }
}
