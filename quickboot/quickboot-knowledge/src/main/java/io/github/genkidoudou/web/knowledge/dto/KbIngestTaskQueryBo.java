package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 异步入库任务查询参数。
 */
@Data
@Schema(description = "异步入库任务查询")
public class KbIngestTaskQueryBo {

    @Min(1)
    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "任务状态")
    private String status;
}
