package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * MCP 环境变量展示对象。
 */
@Data
@Schema(description = "MCP 环境变量")
public class KbMcpEnvVo {

    @Schema(description = "环境变量行 ID")
    private Long envId;

    @Schema(description = "环境变量名")
    private String envKey;

    @Schema(description = "值类型：PLAIN / SECRET / ENV_REF")
    private String valueType;

    @Schema(description = "变量值（列表/默认详情脱敏）")
    private String envValue;

    @Schema(description = "排序序号")
    private Integer sortOrder;
}
