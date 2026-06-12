package io.github.genkidoudou.web.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库外部 MCP 管理配置（{@code qc.knowledge.mcp.*}）。
 */
@Data
@ConfigurationProperties(prefix = "qc.knowledge.mcp")
public class KnowledgeMcpProperties {

    /** 是否启用 MCP 管理与运行时客户端。 */
    private boolean enabled = true;

    /** 全局 STDIO 子进程并发上限。 */
    private int maxStdioProcesses = 10;

    /** MCP 客户端缓存 TTL（秒）。 */
    private int clientCacheTtlSeconds = 300;

    /** 连接测试超时（毫秒）。 */
    private int testTimeoutMs = 20_000;

    /** 导出相关配置。 */
    private Export export = new Export();

    /** STDIO 安全配置。 */
    private Stdio stdio = new Stdio();

    @Data
    public static class Export {

        /** 默认导出是否包含明文密钥。 */
        private boolean includeSecrets = false;
    }

    @Data
    public static class Stdio {

        /**
         * 允许的可执行命令白名单（首段 command）；为空时仅校验非空。
         */
        private List<String> allowedCommands = new ArrayList<>(List.of("npx", "node", "cmd.exe", "docker"));
    }
}
