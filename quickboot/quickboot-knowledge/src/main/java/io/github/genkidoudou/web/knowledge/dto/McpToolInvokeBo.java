package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MCP 工具试跑入参。
 */
@Data
@Schema(description = "MCP 工具试跑请求")
public class McpToolInvokeBo {

    @NotNull(message = "MCP ID 不能为空")
    @Min(value = 1, message = "MCP ID 无效")
    @Schema(description = "MCP 主键")
    private Long mcpId;

    @NotBlank(message = "工具名称不能为空")
    @Schema(description = "工具名称")
    private String toolName;

    @Schema(description = "工具入参 JSON 对象")
    private Map<String, Object> arguments = new LinkedHashMap<>();
}
