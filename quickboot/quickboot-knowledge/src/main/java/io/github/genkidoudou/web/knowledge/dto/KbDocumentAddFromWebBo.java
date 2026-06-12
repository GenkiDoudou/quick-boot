package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 网页 URL 录入文档入参。
 */
@Data
@Schema(description = "网页录入文档入参")
public class KbDocumentAddFromWebBo {

    @NotNull(message = "知识库ID不能为空")
    @Min(value = 1, message = "知识库ID无效")
    @Schema(description = "知识库ID")
    private Long kbId;

    @NotBlank(message = "URL不能为空")
    @Size(max = 2048, message = "URL长度不能超过2048")
    @Schema(description = "网页 URL")
    private String url;

    @Size(max = 255, message = "标题长度不能超过255")
    @Schema(description = "可选展示标题")
    private String title;

    @Valid
    @Schema(description = "可选分段配置")
    private SegmentConfigBo segmentConfig;
}
