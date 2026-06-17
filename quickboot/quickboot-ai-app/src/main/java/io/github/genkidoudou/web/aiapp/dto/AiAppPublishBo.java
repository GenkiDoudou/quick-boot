package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 应用发布入参。
 */
@Data
@Schema(description = "AI 应用发布入参")
public class AiAppPublishBo {

    @NotNull(message = "应用ID不能为空")
    @Schema(description = "应用ID")
    private Long appId;
}
