package io.github.genkidoudou.web.knowledge.mcp.runtime;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.constants.McpTestStatus;
import io.github.genkidoudou.web.knowledge.domain.KbMcpServer;
import io.github.genkidoudou.web.knowledge.dto.McpTestResultVo;
import io.github.genkidoudou.web.knowledge.dto.McpToolInfoVo;
import io.github.genkidoudou.web.knowledge.mapper.KbMcpServerMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MCP 连接测试：执行 initialize + listTools 并回写 {@code last_test_*} 字段。
 */
@Component
public class McpConnectionTester {

    private final McpClientManager clientManager;
    private final KbMcpServerMapper serverMapper;

    public McpConnectionTester(McpClientManager clientManager, KbMcpServerMapper serverMapper) {
        this.clientManager = clientManager;
        this.serverMapper = serverMapper;
    }

    /**
     * 对指定 MCP 执行连接测试。
     *
     * @param mcpId MCP 主键
     * @return 测试结果（含工具列表）
     */
    public McpTestResultVo test(Long mcpId) {
        McpTestResultVo result = new McpTestResultVo();
        try {
            clientManager.evict(mcpId);
            McpSyncClient client = clientManager.getClient(mcpId);
            McpSchema.ListToolsResult toolsResult = client.listTools();
            List<McpSchema.Tool> tools = toolsResult.tools() == null ? List.of() : toolsResult.tools();
            List<McpToolInfoVo> toolVos = new ArrayList<>(tools.size());
            for (McpSchema.Tool tool : tools) {
                McpToolInfoVo vo = new McpToolInfoVo();
                vo.setName(tool.name());
                vo.setDescription(tool.description());
                toolVos.add(vo);
            }
            result.setSuccess(true);
            result.setToolCount(tools.size());
            result.setTools(toolVos);
            result.setMessage("连接成功，发现 " + tools.size() + " 个工具");
            updateTestResult(mcpId, McpTestStatus.SUCCESS, result.getMessage());
        } catch (WarningException ex) {
            result.setSuccess(false);
            result.setMessage(ex.getMessage());
            updateTestResult(mcpId, McpTestStatus.FAILED, truncate(ex.getMessage()));
            throw ex;
        } catch (Exception ex) {
            result.setSuccess(false);
            String msg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            result.setMessage(msg);
            updateTestResult(mcpId, McpTestStatus.FAILED, truncate(msg));
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "MCP 连接测试失败: " + msg);
        }
        return result;
    }

    private void updateTestResult(Long mcpId, String status, String message) {
        KbMcpServer upd = new KbMcpServer();
        upd.setMcpId(mcpId);
        upd.setLastTestStatus(status);
        upd.setLastTestMsg(message);
        upd.setLastTestTime(LocalDateTime.now());
        serverMapper.updateById(upd);
    }

    private String truncate(String message) {
        if (message == null) {
            return "";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
