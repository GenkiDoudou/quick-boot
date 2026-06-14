package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 提示词优化结果 VO。
 */
@Data
@Schema(description = "提示词优化结果")
public class AiPromptOptimizeResultVo {

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "优化后的提示词正文")
    private String optimizedContent;

    @Schema(description = "失败原因")
    private String errorMsg;
}
