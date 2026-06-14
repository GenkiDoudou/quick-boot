package io.github.genkidoudou.web.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流画布节点。
 */
@Data
@Schema(description = "工作流节点")
public class GraphNodeDto {

    @Schema(description = "节点 ID，画布内唯一")
    private String id;

    @Schema(description = "节点类型")
    private String type;

    @Schema(description = "画布坐标")
    private Map<String, Object> position = new HashMap<>();

    @Schema(description = "节点配置数据")
    private Map<String, Object> data = new HashMap<>();

    /** 循环体等子画布容器 ID；为空表示主画布节点。 */
    @Schema(description = "父容器节点 ID（如 loop-body）")
    private String parentId;
}
