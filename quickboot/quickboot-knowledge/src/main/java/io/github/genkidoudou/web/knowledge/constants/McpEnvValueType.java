package io.github.genkidoudou.web.knowledge.constants;

/**
 * MCP 环境变量值类型，对应 {@code kb_mcp_env.value_type}。
 */
public final class McpEnvValueType {

    /** 明文存储，运行时原样使用。 */
    public static final String PLAIN = "PLAIN";

    /** SM4 密文存储，运行时解密。 */
    public static final String SECRET = "SECRET";

    /** 仅存环境变量名，运行时从 {@code System.getenv} 解析。 */
    public static final String ENV_REF = "ENV_REF";

    private McpEnvValueType() {
    }
}
