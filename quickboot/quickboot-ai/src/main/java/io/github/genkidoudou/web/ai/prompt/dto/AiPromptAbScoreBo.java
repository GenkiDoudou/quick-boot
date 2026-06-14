package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * A/B 对比人工评分入参。
 */
@Data
@Schema(description = "A/B 对比评分入参")
public class AiPromptAbScoreBo {

    @NotNull(message = "运行 ID 不能为空")
    @Schema(description = "A/B 运行 ID")
    private Long runId;

    @NotNull(message = "评分 A 不能为空")
    @Min(value = 1, message = "评分 A 须在 1–5 之间")
    @Max(value = 5, message = "评分 A 须在 1–5 之间")
    @Schema(description = "版本 A 评分（1–5）")
    private Integer scoreA;

    @NotNull(message = "评分 B 不能为空")
    @Min(value = 1, message = "评分 B 须在 1–5 之间")
    @Max(value = 5, message = "评分 B 须在 1–5 之间")
    @Schema(description = "版本 B 评分（1–5）")
    private Integer scoreB;

    @NotNull(message = "胜者不能为空")
    @Pattern(regexp = "A|B|TIE", message = "winner 须为 A / B / TIE")
    @Schema(description = "胜者：A / B / TIE")
    private String winner;

    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(description = "评分备注")
    private String remark;
}
