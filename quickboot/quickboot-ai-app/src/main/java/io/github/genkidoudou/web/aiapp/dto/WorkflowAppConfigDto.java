package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 高级编排应用 config_json 结构（{@code app_type=workflow}）。
 */
@Data
@Schema(description = "高级编排应用配置")
public class WorkflowAppConfigDto {

    @Schema(description = "绑定的工作流 ID")
    private Long workflowId;

    @Schema(description = "开场白")
    private String openingMessage;

    @Schema(description = "预设问题")
    private List<String> suggestedQuestions = new ArrayList<>();

    @Schema(description = "是否允许多会话")
    private Boolean multiSession;
}
