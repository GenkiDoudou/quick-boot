package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 检索历史分页查询。
 */
@Data
@Schema(description = "检索历史查询")
public class KbRetrievalLogQueryBo {

    @NotNull(message = "知识库ID不能为空")
    @Schema(description = "知识库ID")
    private Long kbId;

    @Min(value = 1, message = "页码无效")
    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数无效")
    @Schema(description = "每页条数")
    private Integer pageSize = 20;
}
