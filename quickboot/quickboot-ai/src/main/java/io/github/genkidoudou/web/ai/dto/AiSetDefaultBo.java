package io.github.genkidoudou.web.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设置全局默认模型入参。
 */
@Data
@Schema(description = "设置全局默认模型")
public class AiSetDefaultBo {

    @NotNull(message = "模型 ID 不能为空")
    @Schema(description = "模型 ID")
    private Long modelId;

    @NotBlank(message = "defaultSlot 不能为空")
    @Schema(description = "默认槽位：CHAT / EMBEDDING / WORKFLOW_CHAT")
    private String defaultSlot;
}
