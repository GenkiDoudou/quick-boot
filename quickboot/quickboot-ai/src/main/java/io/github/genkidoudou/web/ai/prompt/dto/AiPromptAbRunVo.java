package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A/B 对比运行结果 VO。
 */
@Data
@Schema(description = "A/B 对比运行结果")
public class AiPromptAbRunVo {

    @Schema(description = "运行 ID")
    private Long runId;

    @Schema(description = "提示词 ID")
    private Long promptId;

    @Schema(description = "模型输出 A")
    private String outputA;

    @Schema(description = "模型输出 B")
    private String outputB;

    @Schema(description = "渲染后 prompt A")
    private String renderedPromptA;

    @Schema(description = "渲染后 prompt B")
    private String renderedPromptB;

    @Schema(description = "评分 A")
    private Integer scoreA;

    @Schema(description = "评分 B")
    private Integer scoreB;

    @Schema(description = "胜者")
    private String winner;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
