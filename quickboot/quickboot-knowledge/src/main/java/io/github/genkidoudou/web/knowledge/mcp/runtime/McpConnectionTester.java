package io.github.genkidoudou.web.knowledge.mcp.runtime;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.constants.McpTestStatus;
import io.github.genkidoudou.web.knowledge.domain.KbMcpServer;
import io.github.genkidoudou.web.knowledge.dto.McpTestResultVo;
import io.github.genkidoudou.web.knowledge.dto.McpToolInfoVo;
import io.github.genkidoudou.web.knowledge.mapper.KbMcpServerMapper;
import io.github.genkidoudou.web.knowledge.mcp.support.McpToolInfoSupport;
import io.github.genkidoudou.web.knowledge.mcp.support.McpTransportUrlSupport;
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
     * 拉取 MCP 工具列表（不抛业务异常，供详情弹窗展示）。
     *
     * @param mcpId MCP 主键
     * @return 含工具列表的结果；失败时 {@code success=false} 且 {@code message} 为原因
     */
    public McpTestResultVo listTools(Long mcpId) {
        return fetchTools(mcpId);
    }

    /**
     * 对指定 MCP 执行连接测试。
     *
     * @param mcpId MCP 主键
     * @return 测试结果（含工具列表）
     */
    public McpTestResultVo test(Long mcpId) {
        McpTestResultVo result = fetchTools(mcpId);
        if (!result.isSuccess()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "MCP 连接测试失败: " + result.getMessage());
        }
        return result;
    }

    private McpTestResultVo fetchTools(Long mcpId) {
        McpTestResultVo result = new McpTestResultVo();
        KbMcpServer server = serverMapper.selectById(mcpId);
        try {
            if (server == null) {
                result.setSuccess(false);
                result.setMessage("MCP 配置不存在或已删除");
                return result;
            }
            String mismatch = McpTransportUrlSupport.transportMismatchHint(server.getTransport(), server.getUrl());
            if (mismatch != null) {
                result.setSuccess(false);
                result.setMessage(mismatch);
                updateTestResult(mcpId, McpTestStatus.FAILED, truncate(mismatch), null);
                return result;
            }
            clientManager.evict(mcpId);
            McpSyncClient client = clientManager.getClient(mcpId);
            McpSchema.ListToolsResult toolsResult = client.listTools();
            List<McpSchema.Tool> tools = toolsResult.tools() == null ? List.of() : toolsResult.tools();
            List<McpToolInfoVo> toolVos = new ArrayList<>(tools.size());
            for (McpSchema.Tool tool : tools) {
                toolVos.add(McpToolInfoSupport.toVo(tool));
            }
            result.setSuccess(true);
            result.setToolCount(tools.size());
            result.setTools(toolVos);
            result.setMessage("连接成功，发现 " + tools.size() + " 个工具");
            updateTestResult(mcpId, McpTestStatus.SUCCESS, result.getMessage(), tools.size());
        } catch (Exception ex) {
            result.setSuccess(false);
            String transport = server != null ? server.getTransport() : null;
            String url = server != null ? server.getUrl() : null;
            String msg = McpTransportUrlSupport.enrichFailureMessage(transport, url, resolveErrorMessage(ex));
            result.setMessage(msg);
            updateTestResult(mcpId, McpTestStatus.FAILED, truncate(msg), null);
        }
        return result;
    }

    private static String resolveErrorMessage(Throwable ex) {
        Throwable cur = ex;
        String best = null;
        while (cur != null) {
            String msg = cur.getMessage();
            if (StrUtil.isNotBlank(msg) && !"Client failed to initialize by explicit API call".equals(msg)) {
                best = msg;
            }
            cur = cur.getCause();
        }
        if (best != null) {
            return best;
        }
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }

    private void updateTestResult(Long mcpId, String status, String message, Integer toolCount) {
        KbMcpServer upd = new KbMcpServer();
        upd.setMcpId(mcpId);
        upd.setLastTestStatus(status);
        upd.setLastTestMsg(message);
        upd.setLastTestTime(LocalDateTime.now());
        upd.setToolCount(toolCount);
        serverMapper.updateById(upd);
    }

    private String truncate(String message) {
        if (message == null) {
            return "";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
