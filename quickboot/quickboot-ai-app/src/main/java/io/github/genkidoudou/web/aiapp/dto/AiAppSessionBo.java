package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新建 AI 应用会话入参。
 */
@Data
@Schema(description = "AI 应用会话入参")
public class AiAppSessionBo {

    @NotNull(message = "应用ID不能为空")
    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "会话标题，可选")
    private String title;
}
