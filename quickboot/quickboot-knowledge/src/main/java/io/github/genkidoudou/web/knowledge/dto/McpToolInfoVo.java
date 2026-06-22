package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具元信息（连接测试 / 工具列表展示用）。
 */
@Data
@Schema(description = "MCP 工具信息")
public class McpToolInfoVo {

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具展示标题")
    private String title;

    @Schema(description = "工具描述")
    private String description;

    @Schema(description = "入参字段列表（由 inputSchema 解析）")
    private List<McpToolParamVo> parameters = new ArrayList<>();

    @Schema(description = "完整 inputSchema（JSON Schema）")
    private Map<String, Object> inputSchema;

    @Schema(description = "outputSchema（若服务端提供）")
    private Map<String, Object> outputSchema;
}
