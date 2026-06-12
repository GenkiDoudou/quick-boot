package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工作流画布连线。
 */
@Data
@Schema(description = "工作流连线")
public class GraphEdgeDto {

    @Schema(description = "连线 ID")
    private String id;

    @Schema(description = "源节点 ID")
    private String source;

    @Schema(description = "目标节点 ID")
    private String target;

    @Schema(description = "源出口 handle（if-else / question-classifier 分支标识）")
    private String sourceHandle;
}
