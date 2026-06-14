package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * A/B 对比运行入参。
 */
@Data
@Schema(description = "A/B 对比运行入参")
public class AiPromptAbRunBo {

    @NotNull(message = "提示词 ID 不能为空")
    @Schema(description = "提示词 ID")
    private Long promptId;

    @NotNull(message = "版本 A 不能为空")
    @Schema(description = "版本 A ID（0 表示当前草稿）")
    private Long variantAVersionId;

    @NotNull(message = "版本 B 不能为空")
    @Schema(description = "版本 B ID（0 表示当前草稿）")
    private Long variantBVersionId;

    @Schema(description = "样例变量键值（一层 {{key}} 替换）")
    private Map<String, Object> sampleInputJson;

    @Schema(description = "可选 Chat 模型 ID")
    private Long modelId;
}
