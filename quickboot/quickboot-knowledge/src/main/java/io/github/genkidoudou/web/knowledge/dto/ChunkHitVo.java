package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 语义检索命中片段出参。
 */
@Data
@Schema(description = "检索命中片段")
public class ChunkHitVo {

    @Schema(description = "分块ID")
    private Long chunkId;

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "片段内容")
    private String content;

    @Schema(description = "综合得分（混合检索时为融合分）")
    private Double score;

    @Schema(description = "向量相似度得分")
    private Double vectorScore;

    @Schema(description = "关键词匹配得分")
    private Double keywordScore;

    @Schema(description = "检索模式：VECTOR/HYBRID")
    private String searchMode;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "页码")
    private Integer pageNumber;
}
