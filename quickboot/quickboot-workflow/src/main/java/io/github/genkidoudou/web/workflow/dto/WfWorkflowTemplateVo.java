package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流模板列表/详情出参。
 */
@Data
@Schema(description = "工作流模板")
public class WfWorkflowTemplateVo {

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "唯一编码")
    private String code;

    @Schema(description = "展示名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "图 DSL（详情接口返回）")
    private WorkflowGraphDto graph;

    @Schema(description = "是否内置：0否 1是")
    private Integer builtin;

    @Schema(description = "状态：ENABLED / DISABLED")
    private String status;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
