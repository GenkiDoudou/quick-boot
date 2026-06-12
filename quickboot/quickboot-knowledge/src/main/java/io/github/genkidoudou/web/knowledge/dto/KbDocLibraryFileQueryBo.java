package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文档库文件分页查询入参。
 */
@Data
@Schema(description = "文档库文件查询入参")
public class KbDocLibraryFileQueryBo {

    @NotNull(message = "目录ID不能为空")
    @Min(value = 0, message = "目录ID无效")
    @Schema(description = "所属目录ID")
    private Long folderId;

    @Schema(description = "标题关键字")
    private String title;

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;
}
