package io.github.genkidoudou.web.knowledge.mcp.runtime;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.domain.KbMcpServer;
import io.github.genkidoudou.web.knowledge.dto.McpToolInvokeResultVo;
import io.github.genkidoudou.web.knowledge.mapper.KbMcpServerMapper;
import io.github.genkidoudou.web.knowledge.mcp.support.McpTransportUrlSupport;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具试跑：对指定工具发起 {@code tools/call} 并解析返回内容。
 */
@Component
public class McpToolInvoker {

    private final McpClientManager clientManager;
    private final KbMcpServerMapper serverMapper;

    public McpToolInvoker(McpClientManager clientManager, KbMcpServerMapper serverMapper) {
        this.clientManager = clientManager;
        this.serverMapper = serverMapper;
    }

    /**
     * 调用 MCP 工具并返回可读结果（不抛业务异常，由 VO 承载失败信息）。
     *
     * @param mcpId     MCP 主键
     * @param toolName  工具名
     * @param arguments 入参
     * @return 试跑结果
     */
    public McpToolInvokeResultVo invoke(Long mcpId, String toolName, Map<String, Object> arguments) {
        long start = System.currentTimeMillis();
        McpToolInvokeResultVo vo = new McpToolInvokeResultVo();
        KbMcpServer server = serverMapper.selectById(mcpId);
        try {
            if (server == null) {
                vo.setSuccess(false);
                vo.setMessage("MCP 配置不存在或已删除");
                return vo;
            }
            String mismatch = McpTransportUrlSupport.transportMismatchHint(server.getTransport(), server.getUrl());
            if (mismatch != null) {
                vo.setSuccess(false);
                vo.setMessage(mismatch);
                return vo;
            }
            if (StrUtil.isBlank(toolName)) {
                vo.setSuccess(false);
                vo.setMessage("工具名称不能为空");
                return vo;
            }
            Map<String, Object> args = arguments == null ? Map.of() : arguments;
            McpSyncClient client = clientManager.getClient(mcpId);
            McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest(toolName.trim(), args));
            List<String> texts = extractTexts(result.content());
            vo.setSuccess(true);
            vo.setIsError(Boolean.TRUE.equals(result.isError()));
            vo.setContentTexts(texts);
            vo.setTextOutput(String.join("\n", texts));
            vo.setStructuredContent(result.structuredContent());
            if (Boolean.TRUE.equals(result.isError())) {
                vo.setMessage(StrUtil.blankToDefault(vo.getTextOutput(), "工具执行失败"));
            } else {
                vo.setMessage("执行成功");
            }
        } catch (Exception ex) {
            vo.setSuccess(false);
            String transport = server != null ? server.getTransport() : null;
            String url = server != null ? server.getUrl() : null;
            vo.setMessage(McpTransportUrlSupport.enrichFailureMessage(transport, url, resolveErrorMessage(ex)));
        } finally {
            vo.setDurationMs(System.currentTimeMillis() - start);
        }
        return vo;
    }

    private static List<String> extractTexts(List<McpSchema.Content> contents) {
        if (contents == null || contents.isEmpty()) {
            return List.of();
        }
        List<String> texts = new ArrayList<>(contents.size());
        for (McpSchema.Content content : contents) {
            if (content instanceof McpSchema.TextContent textContent) {
                if (StrUtil.isNotBlank(textContent.text())) {
                    texts.add(textContent.text());
                }
            } else if (content != null) {
                texts.add(String.valueOf(content));
            }
        }
        return texts;
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
}
