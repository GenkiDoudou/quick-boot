package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 采纳 AI 优化结果入参。
 */
@Data
@Schema(description = "采纳优化结果入参")
public class AiPromptAdoptBo {

    @NotNull(message = "会话 ID 不能为空")
    @Schema(description = "优化会话 ID")
    private Long sessionId;
}
