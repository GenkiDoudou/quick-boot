package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自定义内容段配置项（CUSTOM 类型扩展用）。
 */
@Data
@Schema(description = "提示词内容段配置项")
public class AiPromptSectionItem {

    @Schema(description = "段键名")
    private String key;

    @Schema(description = "段展示标签")
    private String label;
}
