package io.github.genkidoudou.web.ai.constants;

import cn.hutool.core.util.StrUtil;

import java.util.Locale;
import java.util.Set;

/**
 * AI 模型类型，对应 {@code ai_model.model_type}。
 */
public final class AiModelType {

    /** 语言模型（对话 / 补全）。 */
    public static final String LANGUAGE = "LANGUAGE";

    /** 图像模型（多模态 / 生图，运行时待扩展）。 */
    public static final String IMAGE = "IMAGE";

    /** 向量模型（Embedding）。 */
    public static final String VECTOR = "VECTOR";

    /** 兼容旧数据：对话模型。 */
    public static final String CHAT = "CHAT";

    /** 兼容旧数据：向量嵌入。 */
    public static final String EMBEDDING = "EMBEDDING";

    private static final Set<String> LANGUAGE_TYPES = Set.of(LANGUAGE, CHAT);
    private static final Set<String> VECTOR_TYPES = Set.of(VECTOR, EMBEDDING);

    private AiModelType() {
    }

    /**
     * 是否为语言模型类型。
     *
     * @param modelType 库表 model_type
     * @return 是否语言模型
     */
    public static boolean isLanguage(String modelType) {
        return modelType != null && LANGUAGE_TYPES.contains(modelType.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * 是否为向量模型类型。
     *
     * @param modelType 库表 model_type
     * @return 是否向量模型
     */
    public static boolean isVector(String modelType) {
        return modelType != null && VECTOR_TYPES.contains(modelType.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * 是否为图像模型类型。
     *
     * @param modelType 库表 model_type
     * @return 是否图像模型
     */
    public static boolean isImage(String modelType) {
        return IMAGE.equals(normalize(modelType));
    }

    /**
     * 是否为当前支持的模型类型枚举值。
     *
     * @param modelType 入参
     * @return 是否支持
     */
    public static boolean isSupported(String modelType) {
        String normalized = normalize(modelType);
        return LANGUAGE.equals(normalized) || IMAGE.equals(normalized) || VECTOR.equals(normalized)
            || CHAT.equals(normalized) || EMBEDDING.equals(normalized);
    }

    /**
     * 列表/下拉过滤：LANGUAGE 或 CHAT 均视为语言模型类。
     *
     * @param filterType 查询参数
     * @return 是否按语言模型过滤
     */
    public static boolean isLanguageFilter(String filterType) {
        String f = normalize(filterType);
        return LANGUAGE.equals(f) || CHAT.equals(f);
    }

    /**
     * 列表/下拉过滤：VECTOR 或 EMBEDDING 均视为向量模型类。
     *
     * @param filterType 查询参数
     * @return 是否按向量模型过滤
     */
    public static boolean isVectorFilter(String filterType) {
        String f = normalize(filterType);
        return VECTOR.equals(f) || EMBEDDING.equals(f);
    }

    private static String normalize(String modelType) {
        return modelType == null ? "" : modelType.trim().toUpperCase(Locale.ROOT);
    }
}
