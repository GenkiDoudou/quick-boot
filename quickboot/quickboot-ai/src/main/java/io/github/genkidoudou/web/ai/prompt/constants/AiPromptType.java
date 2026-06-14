package io.github.genkidoudou.web.ai.prompt.constants;

import cn.hutool.core.util.StrUtil;

import java.util.Locale;
import java.util.Set;

/**
 * 提示词场景类型，对应 {@code ai_prompt.prompt_type}。
 */
public final class AiPromptType {

    /** 工作流大模型节点对齐。 */
    public static final String LLM = "LLM";

    /** 知识库问答对齐。 */
    public static final String RAG = "RAG";

    /** 问题分类对齐。 */
    public static final String CLASSIFIER = "CLASSIFIER";

    /** 参数抽取对齐。 */
    public static final String EXTRACTOR = "EXTRACTOR";

    /** 可扩展自定义段。 */
    public static final String CUSTOM = "CUSTOM";

    private static final Set<String> SUPPORTED = Set.of(LLM, RAG, CLASSIFIER, EXTRACTOR, CUSTOM);

    private AiPromptType() {
    }

    /**
     * 是否为支持的提示词类型。
     *
     * @param promptType 类型值
     * @return 是否支持
     */
    public static boolean isSupported(String promptType) {
        return promptType != null && SUPPORTED.contains(promptType.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * 规范化类型枚举值。
     *
     * @param promptType 入参
     * @return 大写类型；空串时返回空
     */
    public static String normalize(String promptType) {
        return StrUtil.isBlank(promptType) ? "" : promptType.trim().toUpperCase(Locale.ROOT);
    }
}
