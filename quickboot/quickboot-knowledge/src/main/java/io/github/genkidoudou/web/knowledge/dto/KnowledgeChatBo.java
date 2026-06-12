package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * RAG 问答请求入参。
 */
@Data
@Schema(description = "RAG 问答请求")
public class KnowledgeChatBo {

    @NotNull(message = "知识库ID不能为空")
    @Schema(description = "知识库ID")
    private Long kbId;

    @NotBlank(message = "问题不能为空")
    @Size(max = 4000, message = "问题长度不能超过4000")
    @Schema(description = "用户问题")
    private String question;

    @Schema(description = "检索 topK，不传则使用配置默认值")
    private Integer topK;

    @Schema(description = "相似度阈值，不传则使用配置默认值")
    private Double similarityThreshold;

    @Schema(description = "是否启用知识库绑定的 MCP 工具，默认 true")
    private Boolean useMcpTools = true;
}
