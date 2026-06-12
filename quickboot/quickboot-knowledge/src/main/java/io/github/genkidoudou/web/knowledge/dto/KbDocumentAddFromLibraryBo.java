package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 从文档库选取文件入库入参。
 */
@Data
@Schema(description = "文档库选取入库入参")
public class KbDocumentAddFromLibraryBo {

    @NotNull(message = "知识库ID不能为空")
    @Min(value = 1, message = "知识库ID无效")
    @Schema(description = "知识库ID")
    private Long kbId;

    @NotNull(message = "文档库文件ID不能为空")
    @Min(value = 1, message = "文档库文件ID无效")
    @Schema(description = "文档库文件 libFileId")
    private Long libFileId;

    @Size(max = 255, message = "标题长度不能超过255")
    @Schema(description = "可选展示标题（缺省取文档库标题）")
    private String title;

    @Valid
    @Schema(description = "可选分段配置")
    private SegmentConfigBo segmentConfig;
}
