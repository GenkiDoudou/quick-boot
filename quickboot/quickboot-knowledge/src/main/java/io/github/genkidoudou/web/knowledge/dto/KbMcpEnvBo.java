package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * MCP 环境变量新增/修改入参。
 */
@Data
@Schema(description = "MCP 环境变量入参")
public class KbMcpEnvBo {

    @Schema(description = "环境变量行 ID（修改时可选）")
    private Long envId;

    @NotBlank(message = "环境变量名不能为空")
    @Size(max = 128, message = "环境变量名长度不能超过128")
    @Schema(description = "环境变量名")
    private String envKey;

    @NotBlank(message = "值类型不能为空")
    @Schema(description = "值类型：PLAIN / SECRET / ENV_REF")
    private String valueType;

    @Schema(description = "变量值；SECRET 类型修改时留空表示不修改原值")
    private String envValue;

    @Schema(description = "排序序号")
    private Integer sortOrder;
}
