package io.github.genkidoudou.web.knowledge.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库新增/修改入参。
 */
@Data
@Schema(description = "知识库业务入参")
public class KbKnowledgeBaseBo {

    @NotNull(message = "知识库ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "知识库ID（修改必填）")
    private Long kbId;

    @NotBlank(message = "知识库名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "知识库名称长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "知识库名称")
    private String name;

    @Size(max = 500, message = "描述长度不能超过500", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "描述")
    private String description;

    @Min(value = 128, message = "分块大小不能小于128", groups = {AddGroup.class, UpdateGroup.class})
    @Max(value = 4096, message = "分块大小不能大于4096", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "分块 token 上限")
    private Integer chunkSize;

    @Min(value = 0, message = "分块重叠不能为负", groups = {AddGroup.class, UpdateGroup.class})
    @Max(value = 512, message = "分块重叠不能大于512", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "分块重叠 token 数")
    private Integer chunkOverlap;

    @Schema(description = "默认分段模式：AUTO / CUSTOM")
    private String segmentMode;

    @Schema(description = "默认分隔符：SINGLE_NEWLINE / DOUBLE_NEWLINE")
    private String chunkDelimiter;

    @Schema(description = "默认预处理：归一化连续空白")
    private Boolean preprocessNormalizeWs;

    @Schema(description = "默认预处理：删除 URL")
    private Boolean preprocessRemoveUrl;

    @Schema(description = "默认预处理：删除电子邮箱")
    private Boolean preprocessRemoveEmail;

    @Min(value = 0, message = "状态值无效", groups = {AddGroup.class, UpdateGroup.class})
    @Max(value = 1, message = "状态值无效", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "状态：0 正常 / 1 停用")
    private Integer status;

    @Schema(description = "关联的外部 MCP ID 列表")
    private java.util.List<Long> mcpIds;

    @Schema(description = "可选 Chat 模型 ID")
    private Long chatModelId;

    @Schema(description = "可选 Embedding 模型 ID")
    private Long embeddingModelId;
}
