package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 内置工作流模板出参。
 */
@Data
@Schema(description = "内置工作流模板")
public class WfTemplateVo {

    @Schema(description = "模板编码")
    private String code;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "图 DSL")
    private WorkflowGraphDto graph;
}
