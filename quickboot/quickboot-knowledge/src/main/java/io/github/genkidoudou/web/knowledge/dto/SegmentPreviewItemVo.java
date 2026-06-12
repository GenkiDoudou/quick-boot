package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分段预览单项。
 */
@Data
@Schema(description = "分段预览项")
public class SegmentPreviewItemVo {

    @Schema(description = "分块序号，从 0 起")
    private Integer chunkIndex;

    @Schema(description = "分块正文")
    private String content;

    @Schema(description = "估算 token 数")
    private Integer tokenCount;

    @Schema(description = "页码（PDF 等）")
    private Integer pageNumber;
}
