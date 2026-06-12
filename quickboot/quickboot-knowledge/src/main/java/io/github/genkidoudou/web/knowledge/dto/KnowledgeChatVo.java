package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * RAG 问答响应出参。
 */
@Data
@Schema(description = "RAG 问答响应")
public class KnowledgeChatVo {

    @Schema(description = "模型生成的回答")
    private String answer;

    @Schema(description = "引用片段列表")
    private List<CitationVo> citations;

    @Schema(description = "本次问答实际调用的 MCP 工具名列表")
    private List<String> mcpToolsUsed;
}
