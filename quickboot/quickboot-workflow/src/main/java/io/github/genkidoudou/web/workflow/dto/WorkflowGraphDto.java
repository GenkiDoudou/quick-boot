package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 工作流 DAG JSON DSL 顶层结构。
 */
@Data
@Schema(description = "工作流图 DSL")
public class WorkflowGraphDto {

    @Schema(description = "DSL 版本号")
    private Integer version = 1;

    @Schema(description = "节点列表")
    private List<GraphNodeDto> nodes = new ArrayList<>();

    @Schema(description = "连线列表")
    private List<GraphEdgeDto> edges = new ArrayList<>();
}
