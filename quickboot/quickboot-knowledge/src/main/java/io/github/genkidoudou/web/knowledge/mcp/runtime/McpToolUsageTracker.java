package io.github.genkidoudou.web.knowledge.mcp.runtime;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 线程级 MCP 工具调用记录，供 RAG / 工作流响应填充 {@code mcpToolsUsed} 与 {@code mcpToolResults}。
 */
@Component
public class McpToolUsageTracker {

    private final ThreadLocal<List<McpToolInvocationRecord>> invocations = ThreadLocal.withInitial(ArrayList::new);

    /**
     * 记录一次工具调用（含入参与原始返回）。
     *
     * @param toolName 工具名称
     * @param input    工具入参 JSON 字符串
     * @param output   工具原始返回
     */
    public void recordInvocation(String toolName, String input, String output) {
        if (toolName == null || toolName.isBlank()) {
            return;
        }
        invocations.get().add(new McpToolInvocationRecord(
            toolName,
            input == null ? "" : input,
            output == null ? "" : output
        ));
    }

    /**
     * 获取当前线程已记录的工具名列表（副本）。
     *
     * @return 工具名列表
     */
    public List<String> getUsedTools() {
        return invocations.get().stream().map(McpToolInvocationRecord::toolName).toList();
    }

    /**
     * 获取当前线程调用记录（副本）。
     *
     * @return 调用记录列表
     */
    public List<McpToolInvocationRecord> getInvocations() {
        return List.copyOf(invocations.get());
    }

    /**
     * 清空当前线程记录（问答 / 节点执行开始前调用）。
     */
    public void clear() {
        invocations.remove();
    }

    /**
     * 获取并清空当前线程的完整调用记录。
     *
     * @return 不可变调用记录列表
     */
    public List<McpToolInvocationRecord> drainInvocations() {
        List<McpToolInvocationRecord> snapshot = getInvocations();
        clear();
        return Collections.unmodifiableList(snapshot);
    }

    /**
     * 获取并清空当前线程记录的工具名列表（RAG 等仅需工具名场景）。
     *
     * @return 不可变工具名列表
     */
    public List<String> drain() {
        return drainInvocations().stream().map(McpToolInvocationRecord::toolName).toList();
    }
}
