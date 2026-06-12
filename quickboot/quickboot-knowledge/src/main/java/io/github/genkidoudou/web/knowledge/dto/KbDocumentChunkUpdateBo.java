package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文档分块更新请求。
 */
@Data
@Schema(description = "文档分块更新")
public class KbDocumentChunkUpdateBo {

    @NotNull(message = "分块ID不能为空")
    @Schema(description = "分块ID")
    private Long chunkId;

    @Size(max = 32000, message = "分块正文不能超过32000字符")
    @Schema(description = "分块正文，不传则不修改")
    private String content;

    @Min(value = 0, message = "启用状态无效")
    @Max(value = 1, message = "启用状态无效")
    @Schema(description = "是否启用：0禁用 1启用，不传则不修改")
    private Integer enabled;
}
