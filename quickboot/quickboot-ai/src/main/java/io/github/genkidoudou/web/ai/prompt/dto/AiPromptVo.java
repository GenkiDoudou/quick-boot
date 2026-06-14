package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提示词详情/列表 VO。
 */
@Data
@Schema(description = "提示词视图对象")
public class AiPromptVo {

    @Schema(description = "提示词 ID")
    private Long promptId;

    @Schema(description = "提示词名称")
    private String name;

    @Schema(description = "提示词分类")
    private String category;

    @Schema(description = "提示词描述")
    private String description;

    @Schema(description = "提示词内容")
    private String content;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
