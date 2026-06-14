package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工作流模板分页查询入参。
 */
@Data
@Schema(description = "工作流模板查询")
public class WfWorkflowTemplateQueryBo {

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;

    @Schema(description = "名称模糊")
    private String name;

    @Schema(description = "编码模糊")
    private String code;

    @Schema(description = "状态：ENABLED / DISABLED")
    private String status;
}
