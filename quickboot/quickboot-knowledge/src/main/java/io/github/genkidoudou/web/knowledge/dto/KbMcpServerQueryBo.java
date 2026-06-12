package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * MCP 服务分页查询参数。
 */
@Data
@Schema(description = "MCP 服务分页查询")
public class KbMcpServerQueryBo {

    @Min(1)
    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "名称，模糊匹配")
    private String name;

    @Schema(description = "编码，模糊匹配")
    private String code;

    @Schema(description = "传输方式：STDIO / SSE / STREAMABLE_HTTP")
    private String transport;

    @Schema(description = "状态：0 正常 / 1 停用")
    private Integer status;
}
