package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库列表/详情出参。
 */
@Data
@Schema(description = "知识库视图对象")
public class KbKnowledgeBaseVo {

    @Schema(description = "知识库ID")
    private Long kbId;

    @Schema(description = "知识库名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "分块 token 上限")
    private Integer chunkSize;

    @Schema(description = "分块重叠 token 数")
    private Integer chunkOverlap;

    @Schema(description = "默认分段模式：AUTO / CUSTOM")
    private String segmentMode;

    @Schema(description = "默认分隔符：SINGLE_NEWLINE / DOUBLE_NEWLINE")
    private String chunkDelimiter;

    @Schema(description = "默认预处理：归一化连续空白 0/1")
    private Integer preprocessNormalizeWs;

    @Schema(description = "默认预处理：删除 URL 0/1")
    private Integer preprocessRemoveUrl;

    @Schema(description = "默认预处理：删除电子邮箱 0/1")
    private Integer preprocessRemoveEmail;

    @Schema(description = "状态：0 正常 / 1 停用")
    private Integer status;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "关联的外部 MCP ID 列表")
    private java.util.List<Long> mcpIds;

    @Schema(description = "可选 Chat 模型 ID")
    private Long chatModelId;

    @Schema(description = "可选 Embedding 模型 ID")
    private Long embeddingModelId;
}
