package io.github.genkidoudou.web.ai.support;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;

/**
 * Embedding 向量维度校验，须与 {@code qc.ai.vectorDimensions}（对齐 {@code qc.knowledge.vectorDimensions}）一致。
 */
public final class AiDimensionValidator {

    private AiDimensionValidator() {
    }

    /**
     * 校验模型配置的 Embedding 维度是否与期望值一致。
     *
     * @param modelDimensions    模型表字段 dimensions
     * @param expectedDimensions 期望维度（通常来自 qc.ai.vectorDimensions）
     */
    public static void validate(int modelDimensions, int expectedDimensions) {
        if (modelDimensions != expectedDimensions) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "Embedding 维度 " + modelDimensions + " 与系统配置 " + expectedDimensions
                    + " 不一致，请调整模型 dimensions 或 vectorDimensions");
        }
    }

    /**
     * 校验模型维度非空且与期望值一致。
     *
     * @param modelDimensions    模型维度，可为 null
     * @param expectedDimensions 期望维度
     */
    public static void validateRequired(Integer modelDimensions, int expectedDimensions) {
        if (modelDimensions == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "Embedding 模型必须配置 dimensions");
        }
        validate(modelDimensions, expectedDimensions);
    }
}
