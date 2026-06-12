package io.github.genkidoudou.web.ai.constants;

/**
 * AI 模块通用常量。
 */
public final class AiConstants {

    /** 模型状态：正常可用。 */
    public static final int STATUS_NORMAL = 0;

    /** 模型状态：停用。 */
    public static final int STATUS_DISABLED = 1;

    /** 逻辑删除：未删除。 */
    public static final int NOT_DELETED = 0;

    /** 逻辑删除：已删除。 */
    public static final int DELETED = 1;

    private AiConstants() {
    }
}
