package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工作流分页查询入参。
 */
@Data
@Schema(description = "工作流查询入参")
public class WfWorkflowQueryBo {

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;

    @Schema(description = "名称模糊匹配")
    private String name;

    @Schema(description = "状态：DRAFT/PUBLISHED/DISABLED")
    private String status;
}
