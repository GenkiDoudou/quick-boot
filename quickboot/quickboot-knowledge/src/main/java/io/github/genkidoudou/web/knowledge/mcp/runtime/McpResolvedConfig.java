package io.github.genkidoudou.web.knowledge.mcp.runtime;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行时解析后的 MCP 服务配置（含解密后的环境变量与请求头）。
 */
@Data
@Builder
public class McpResolvedConfig {

    /** MCP 主键。 */
    private Long mcpId;

    /** 唯一编码。 */
    private String code;

    /** 传输方式：STDIO / SSE / STREAMABLE_HTTP。 */
    private String transport;

    /** STDIO 命令。 */
    private String command;

    @Builder.Default
    private List<String> args = new ArrayList<>();

    /** 远程 URL。 */
    private String url;

    @Builder.Default
    private Map<String, String> headers = new LinkedHashMap<>();

    @Builder.Default
    private Map<String, String> env = new LinkedHashMap<>();

    /** 请求超时毫秒。 */
    private int requestTimeoutMs;
}
