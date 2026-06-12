package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流列表/详情出参。
 */
@Data
@Schema(description = "工作流出参")
public class WfWorkflowVo {

    @Schema(description = "工作流ID")
    private Long workflowId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "当前发布版本ID")
    private Long publishedVersionId;

    @Schema(description = "可选 Chat 模型 ID")
    private Long chatModelId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
