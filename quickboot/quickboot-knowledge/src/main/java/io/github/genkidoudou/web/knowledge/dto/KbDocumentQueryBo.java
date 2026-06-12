package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 知识库文档分页查询参数。
 */
@Data
@Schema(description = "知识库文档分页查询")
public class KbDocumentQueryBo {

    @Min(1)
    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "知识库ID，精确匹配")
    private Long kbId;

    @Schema(description = "标题，模糊匹配")
    private String title;

    @Schema(description = "入库状态，精确匹配")
    private String docStatus;
}
