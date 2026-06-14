package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 提示词下拉选项 VO（工作流等场景引用）。
 */
@Data
@Schema(description = "提示词下拉选项")
public class AiPromptOptionVo {

    @Schema(description = "提示词 ID")
    private Long promptId;

    @Schema(description = "提示词名称")
    private String name;

    @Schema(description = "提示词分类")
    private String category;
}
