package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP 工具入参字段（由 {@code inputSchema.properties} 解析）。
 */
@Data
@Schema(description = "MCP 工具入参")
public class McpToolParamVo {

    @Schema(description = "参数名")
    private String name;

    @Schema(description = "JSON Schema 类型，如 string / integer / array")
    private String type;

    @Schema(description = "参数说明")
    private String description;

    @Schema(description = "是否必填")
    private Boolean required;

    @Schema(description = "默认值（字符串化展示）")
    private String defaultValue;

    @Schema(description = "枚举可选值")
    private List<String> enumValues = new ArrayList<>();
}
