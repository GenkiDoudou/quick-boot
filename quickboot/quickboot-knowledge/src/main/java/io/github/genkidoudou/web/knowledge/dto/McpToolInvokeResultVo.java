package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具试跑结果。
 */
@Data
@Schema(description = "MCP 工具试跑结果")
public class McpToolInvokeResultVo {

    @Schema(description = "HTTP/连接层是否成功")
    private boolean success;

    @Schema(description = "MCP 工具是否标记为错误结果")
    private Boolean isError;

    @Schema(description = "摘要信息")
    private String message;

    @Schema(description = "文本内容片段")
    private List<String> contentTexts = new ArrayList<>();

    @Schema(description = "拼接后的文本输出")
    private String textOutput;

    @Schema(description = "结构化输出 structuredContent")
    private Object structuredContent;

    @Schema(description = "耗时毫秒")
    private Long durationMs;
}
