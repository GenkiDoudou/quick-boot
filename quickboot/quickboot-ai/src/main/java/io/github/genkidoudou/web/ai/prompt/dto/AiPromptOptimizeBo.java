package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 提示词优化入参。
 */
@Data
@Schema(description = "提示词优化入参")
public class AiPromptOptimizeBo {

    @NotBlank(message = "提示词内容不能为空")
    @Schema(description = "待优化的提示词正文")
    private String content;

    @Schema(description = "可选 Chat 模型 ID，缺省使用全局默认")
    private Long modelId;
}
