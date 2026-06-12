package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP 连接测试结果。
 */
@Data
@Schema(description = "MCP 连接测试结果")
public class McpTestResultVo {

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "可用工具数量")
    private int toolCount;

    @Schema(description = "探测摘要")
    private String message;

    @Schema(description = "工具列表")
    private List<McpToolInfoVo> tools = new ArrayList<>();
}
