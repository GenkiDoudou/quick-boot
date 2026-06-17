package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 应用分页查询入参。
 */
@Data
@Schema(description = "AI 应用查询入参")
public class AiAppQueryBo {

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;

    @Schema(description = "名称模糊匹配")
    private String name;

    @Schema(description = "应用类型：agent / workflow")
    private String appType;

    @Schema(description = "状态：draft / published")
    private String status;
}
