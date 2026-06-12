package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * RAG 回答引用片段。
 */
@Data
@Schema(description = "RAG 引用片段")
public class CitationVo {

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "分块ID")
    private Long chunkId;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "片段摘要")
    private String contentPreview;

    @Schema(description = "相似度得分")
    private Double score;

    @Schema(description = "页码")
    private Integer pageNumber;
}
