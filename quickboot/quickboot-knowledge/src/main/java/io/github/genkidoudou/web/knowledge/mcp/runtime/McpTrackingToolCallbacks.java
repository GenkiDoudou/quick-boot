package io.github.genkidoudou.web.knowledge.mcp.runtime;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * 包装 {@link ToolCallback}，在调用时记录工具名供 RAG 响应回显。
 */
public final class McpTrackingToolCallbacks {

    private McpTrackingToolCallbacks() {
    }

    /**
     * 为回调数组增加调用追踪。
     *
     * @param callbacks 原始回调
     * @param tracker   线程级记录器
     * @return 包装后的回调数组
     */
    public static ToolCallback[] wrap(ToolCallback[] callbacks, McpToolUsageTracker tracker) {
        if (callbacks == null || callbacks.length == 0) {
            return new ToolCallback[0];
        }
        ToolCallback[] wrapped = new ToolCallback[callbacks.length];
        for (int i = 0; i < callbacks.length; i++) {
            wrapped[i] = wrapOne(callbacks[i], tracker);
        }
        return wrapped;
    }

    private static ToolCallback wrapOne(ToolCallback delegate, McpToolUsageTracker tracker) {
        return new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                return delegate.getToolDefinition();
            }

            @Override
            public String call(String toolInput) {
                ToolDefinition def = delegate.getToolDefinition();
                String toolName = def != null ? def.name() : null;
                String result = delegate.call(toolInput);
                if (toolName != null && !toolName.isBlank()) {
                    tracker.recordInvocation(toolName, toolInput, result);
                }
                return result;
            }
        };
    }
}
