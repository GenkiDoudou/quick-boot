package io.github.genkidoudou.web.ai.prompt.constants;

/**
 * 版本快照来源，对应 {@code ai_prompt_version.source}。
 */
public final class AiPromptVersionSource {

    /** 手工编辑发布。 */
    public static final String EDIT = "EDIT";

    /** AI 优化采纳。 */
    public static final String OPTIMIZE = "OPTIMIZE";

    /** A/B 测试采纳（预留）。 */
    public static final String AB_ADOPT = "AB_ADOPT";

    /** 版本回滚。 */
    public static final String ROLLBACK = "ROLLBACK";

    /** 发布生成。 */
    public static final String PUBLISH = "PUBLISH";

    private AiPromptVersionSource() {
    }
}
