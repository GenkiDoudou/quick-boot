package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分段预览请求（手动 / 网页 / 文档库来源）。
 */
@Data
@Schema(description = "分段预览请求")
public class SegmentPreviewBo {

    @NotNull(message = "知识库ID不能为空")
    @Schema(description = "知识库ID")
    private Long kbId;

    @NotBlank(message = "来源类型不能为空")
    @Schema(description = "来源类型：MANUAL/WEB/LIBRARY")
    private String sourceType;

    @Schema(description = "手动录入标题（MANUAL）")
    private String title;

    @Schema(description = "手动录入正文（MANUAL）")
    private String content;

    @Schema(description = "网页 URL（WEB）")
    private String url;

    @Schema(description = "文档库文件 ID（LIBRARY）")
    private Long libFileId;

    @Schema(description = "可选分段配置")
    private SegmentConfigBo segmentConfig;
}
