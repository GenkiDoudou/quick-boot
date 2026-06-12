package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档分块视图对象。
 */
@Data
@Schema(description = "文档分块视图对象")
public class KbDocumentChunkVo {

    @Schema(description = "分块ID")
    private Long chunkId;

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "分块序号")
    private Integer chunkIndex;

    @Schema(description = "内容摘要")
    private String contentPreview;

    @Schema(description = "完整正文")
    private String content;

    @Schema(description = "向量ID")
    private String vectorId;

    @Schema(description = "token 数")
    private Integer tokenCount;

    @Schema(description = "页码")
    private Integer pageNumber;

    @Schema(description = "是否启用：0禁用 1启用")
    private Integer enabled;
}
