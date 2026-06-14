package io.github.genkidoudou.web.ai.prompt.constants;

import cn.hutool.core.util.StrUtil;

import java.util.Locale;
import java.util.Set;

/**
 * 提示词状态，对应 {@code ai_prompt.status}。
 */
public final class AiPromptStatus {

    /** 草稿，可编辑。 */
    public static final String DRAFT = "DRAFT";

    /** 已发布，内容只读。 */
    public static final String PUBLISHED = "PUBLISHED";

    /** 已停用，不出现在 options。 */
    public static final String ARCHIVED = "ARCHIVED";

    private static final Set<String> SUPPORTED = Set.of(DRAFT, PUBLISHED, ARCHIVED);

    private AiPromptStatus() {
    }

    /**
     * 是否为支持的状态值。
     *
     * @param status 状态
     * @return 是否支持
     */
    public static boolean isSupported(String status) {
        return status != null && SUPPORTED.contains(status.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * 规范化状态枚举值。
     *
     * @param status 入参
     * @return 大写状态
     */
    public static String normalize(String status) {
        return StrUtil.isBlank(status) ? "" : status.trim().toUpperCase(Locale.ROOT);
    }
}
