package io.github.genkidoudou.web.knowledge.mcp.runtime;

/**
 * 单次 MCP 工具调用记录（入参 + 原始返回值），供工作流节点输出 {@code mcpToolResults}。
 *
 * @param toolName 工具名称
 * @param input    模型传入的工具参数（通常为 JSON 字符串）
 * @param output   工具原始返回（未经大模型改写）
 */
public record McpToolInvocationRecord(String toolName, String input, String output) {
}
