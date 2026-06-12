package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * MCP 远程 HTTP 请求头键值项。
 */
@Data
@Schema(description = "MCP HTTP 请求头项")
public class McpHeaderItemBo {

    @NotBlank(message = "请求头名称不能为空")
    @Size(max = 128, message = "请求头名称长度不能超过128")
    @Schema(description = "请求头名称")
    private String name;

    @Schema(description = "请求头值；密钥类型时修改留空表示不修改原值")
    private String value;

    /**
     * 值类型：PLAIN / SECRET / ENV_REF，与 {@code kb_mcp_env.value_type} 语义一致。
     */
    @Schema(description = "值类型：PLAIN / SECRET / ENV_REF")
    private String valueType;
}
