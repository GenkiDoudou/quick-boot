package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 单次文档入库可选分段与预处理配置；字段均可空，缺省继承所属知识库默认。
 */
@Data
@Schema(description = "文档分段与预处理配置（可选，缺省继承知识库）")
public class SegmentConfigBo {

    @Schema(description = "分段模式：AUTO / CUSTOM")
    private String segmentMode;

    @Min(value = 128, message = "分块大小不能小于128")
    @Max(value = 4096, message = "分块大小不能大于4096")
    @Schema(description = "分块 token 上限")
    private Integer chunkSize;

    @Min(value = 0, message = "分块重叠不能为负")
    @Max(value = 512, message = "分块重叠不能大于512")
    @Schema(description = "分块重叠 token 数")
    private Integer chunkOverlap;

    @Schema(description = "自定义分隔符：SINGLE_NEWLINE / DOUBLE_NEWLINE")
    private String chunkDelimiter;

    @Schema(description = "预处理：归一化连续空白")
    private Boolean preprocessNormalizeWs;

    @Schema(description = "预处理：删除 URL")
    private Boolean preprocessRemoveUrl;

    @Schema(description = "预处理：删除电子邮箱")
    private Boolean preprocessRemoveEmail;
}
