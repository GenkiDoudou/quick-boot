package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 手动录入文档入参。
 */
@Data
@Schema(description = "手动录入文档入参")
public class KbDocumentAddManualBo {

    @NotNull(message = "知识库ID不能为空")
    @Min(value = 1, message = "知识库ID无效")
    @Schema(description = "知识库ID")
    private Long kbId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255")
    @Schema(description = "文档标题")
    private String title;

    @NotBlank(message = "正文不能为空")
    @Schema(description = "正文（纯文本或 Markdown）")
    private String content;

    @Valid
    @Schema(description = "可选分段配置")
    private SegmentConfigBo segmentConfig;
}
