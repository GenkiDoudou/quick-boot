package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 同步 Debug 运行入参。
 */
@Data
@Schema(description = "Debug 运行入参")
public class WfRunDebugBo {

    @NotNull(message = "工作流ID不能为空")
    @Schema(description = "工作流ID")
    private Long workflowId;

    @Schema(description = "Start 节点入参")
    private Map<String, Object> inputs = new HashMap<>();

    @Schema(description = "是否使用草稿版本（默认 true）")
    private Boolean useDraft = true;

    @Schema(description = "知识库ID（注入 sys.kbId）")
    private Long kbId;

    @Schema(description = "是否启用 SSE 流式（Debug 同步模式一般 false）")
    private Boolean stream = false;

    @Schema(description = "设计器可选：当前画布 graph DSL，传入时优先于服务端草稿执行")
    private WorkflowGraphDto graph;
}
