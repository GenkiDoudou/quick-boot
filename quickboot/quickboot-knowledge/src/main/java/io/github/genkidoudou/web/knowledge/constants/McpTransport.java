package io.github.genkidoudou.web.knowledge.constants;

/**
 * MCP 客户端传输方式，对应 {@code kb_mcp_server.transport}。
 */
public final class McpTransport {

    /** 本地子进程 STDIO（command + args + env）。 */
    public static final String STDIO = "STDIO";

    /** 远程 HTTP SSE（遗留协议）。 */
    public static final String SSE = "SSE";

    /** 远程 Streamable HTTP。 */
    public static final String STREAMABLE_HTTP = "STREAMABLE_HTTP";

    private McpTransport() {
    }
}
