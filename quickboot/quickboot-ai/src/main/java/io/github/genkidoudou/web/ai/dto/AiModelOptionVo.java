package io.github.genkidoudou.web.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 大模型下拉选项。
 */
@Data
@Schema(description = "AI 大模型下拉选项")
public class AiModelOptionVo {

    @Schema(description = "模型 ID")
    private Long modelId;

    @Schema(description = "展示名称")
    private String name;

    @Schema(description = "唯一编码")
    private String code;

    @Schema(description = "模型类型")
    private String modelType;

    @Schema(description = "厂商")
    private String provider;

    @Schema(description = "默认槽位")
    private String defaultSlot;
}
