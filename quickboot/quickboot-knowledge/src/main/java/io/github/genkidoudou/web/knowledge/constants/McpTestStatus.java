package io.github.genkidoudou.web.knowledge.constants;

/**
 * MCP 连接探测结果状态，对应 {@code kb_mcp_server.last_test_status}。
 */
public final class McpTestStatus {

    /** 最近一次探测成功。 */
    public static final String SUCCESS = "SUCCESS";

    /** 最近一次探测失败。 */
    public static final String FAILED = "FAILED";

    /** 尚未执行过探测。 */
    public static final String UNTESTED = "UNTESTED";

    private McpTestStatus() {
    }
}
