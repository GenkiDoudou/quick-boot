package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * MCP 工具元信息（连接测试展示用）。
 */
@Data
@Schema(description = "MCP 工具信息")
public class McpToolInfoVo {

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具描述")
    private String description;
}
