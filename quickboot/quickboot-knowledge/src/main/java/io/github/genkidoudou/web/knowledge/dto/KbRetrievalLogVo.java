package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检索测试历史视图。
 */
@Data
@Schema(description = "检索测试历史")
public class KbRetrievalLogVo {

    @Schema(description = "记录ID")
    private Long logId;

    @Schema(description = "知识库ID")
    private Long kbId;

    @Schema(description = "检索词")
    private String query;

    @Schema(description = "检索模式")
    private String searchMode;

    @Schema(description = "返回条数")
    private Integer topK;

    @Schema(description = "相似度阈值")
    private Double similarityThreshold;

    @Schema(description = "命中条数")
    private Integer hitCount;

    @Schema(description = "操作人")
    private String createBy;

    @Schema(description = "检索时间")
    private LocalDateTime createTime;
}
