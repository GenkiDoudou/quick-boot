package io.github.genkidoudou.web.knowledge.mcp.runtime;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合多个 MCP 服务的 Spring AI {@link ToolCallback}。
 */
@Component
public class McpToolCallbackProvider {

    private final McpClientManager clientManager;

    public McpToolCallbackProvider(McpClientManager clientManager) {
        this.clientManager = clientManager;
    }

    /**
     * 按 MCP ID 列表聚合工具回调。
     *
     * @param mcpIds 已启用且绑定的 MCP 主键列表
     * @return ToolCallback 数组；无可用工具时返回空数组
     */
    public ToolCallback[] getToolCallbacks(List<Long> mcpIds) {
        if (mcpIds == null || mcpIds.isEmpty()) {
            return new ToolCallback[0];
        }
        List<McpSyncClient> clients = new ArrayList<>(mcpIds.size());
        for (Long mcpId : mcpIds) {
            try {
                clients.add(clientManager.getClient(mcpId));
            } catch (WarningException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                    "加载 MCP 工具失败 mcpId=" + mcpId + ": " + ex.getMessage());
            }
        }
        if (clients.isEmpty()) {
            return new ToolCallback[0];
        }
        return new SyncMcpToolCallbackProvider(clients).getToolCallbacks();
    }
}
