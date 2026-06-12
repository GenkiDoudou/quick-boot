package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 语义检索请求入参。
 */
@Data
@Schema(description = "语义检索请求")
public class KnowledgeSearchBo {

    @NotNull(message = "知识库ID不能为空")
    @Schema(description = "知识库ID")
    private Long kbId;

    @NotBlank(message = "检索词不能为空")
    @Size(max = 2000, message = "检索词长度不能超过2000")
    @Schema(description = "自然语言检索词")
    private String query;

    @Schema(description = "返回条数，不传则使用配置默认值")
    private Integer topK;

    @Schema(description = "相似度阈值，不传则使用配置默认值")
    private Double similarityThreshold;

    @Schema(description = "检索模式：VECTOR/HYBRID，默认 HYBRID")
    private String searchMode;

    @Schema(description = "是否记录检索历史，默认 true")
    private Boolean saveHistory;
}
