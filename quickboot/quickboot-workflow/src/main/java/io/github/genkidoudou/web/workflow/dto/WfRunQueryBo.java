package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 运行记录分页查询入参。
 */
@Data
@Schema(description = "运行记录查询入参")
public class WfRunQueryBo {

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;

    @Schema(description = "工作流ID")
    private Long workflowId;

    @Schema(description = "运行状态")
    private String status;
}
