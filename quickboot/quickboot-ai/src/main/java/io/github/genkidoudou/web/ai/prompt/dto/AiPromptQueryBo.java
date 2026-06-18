package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 提示词分页查询参数。
 */
@Data
@Schema(description = "提示词分页查询")
public class AiPromptQueryBo {

    @Min(1)
    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "名称，模糊匹配")
    private String name;

    @Schema(description = "分类，模糊匹配")
    private String category;
}
