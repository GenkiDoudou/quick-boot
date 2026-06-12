package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * MCP 下拉选项（知识库绑定用）。
 */
@Data
@Schema(description = "MCP 下拉选项")
public class KbMcpOptionVo {

    @Schema(description = "MCP ID")
    private Long mcpId;

    @Schema(description = "展示名称")
    private String name;

    @Schema(description = "唯一编码")
    private String code;

    @Schema(description = "传输方式")
    private String transport;
}
