package io.github.genkidoudou.web.knowledge.mcp.runtime;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 线程级 MCP 工具调用记录，供 RAG 响应填充 {@code mcpToolsUsed}。
 */
@Component
public class McpToolUsageTracker {

    private final ThreadLocal<List<String>> usedTools = ThreadLocal.withInitial(ArrayList::new);

    /**
     * 记录一次工具调用。
     *
     * @param toolName 工具名称
     */
    public void record(String toolName) {
        if (toolName == null || toolName.isBlank()) {
            return;
        }
        usedTools.get().add(toolName);
    }

    /**
     * 获取当前线程已记录的工具名列表（副本）。
     *
     * @return 工具名列表
     */
    public List<String> getUsedTools() {
        return List.copyOf(usedTools.get());
    }

    /**
     * 清空当前线程记录（问答开始前调用）。
     */
    public void clear() {
        usedTools.remove();
    }

    /**
     * 获取并清空当前线程记录。
     *
     * @return 不可变工具名列表
     */
    public List<String> drain() {
        List<String> snapshot = getUsedTools();
        clear();
        return Collections.unmodifiableList(snapshot);
    }
}
