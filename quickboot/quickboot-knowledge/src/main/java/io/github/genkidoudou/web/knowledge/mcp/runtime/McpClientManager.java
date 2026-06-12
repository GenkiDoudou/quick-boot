package io.github.genkidoudou.web.knowledge.mcp.runtime;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.knowledge.config.KnowledgeMcpProperties;
import io.github.genkidoudou.web.knowledge.constants.KnowledgeConstants;
import io.github.genkidoudou.web.knowledge.constants.McpTransport;
import io.github.genkidoudou.web.knowledge.domain.KbMcpEnv;
import io.github.genkidoudou.web.knowledge.domain.KbMcpServer;
import io.github.genkidoudou.web.knowledge.mapper.KbMcpEnvMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbMcpServerMapper;
import io.github.genkidoudou.web.knowledge.mcp.support.McpHeaderSupport;
import io.github.genkidoudou.web.knowledge.mcp.support.McpSecretSupport;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MCP 同步客户端缓存：按 {@code mcp_id} 复用连接，支持 TTL 驱逐与 STDIO 子进程限额。
 */
@Component
public class McpClientManager {

    private final KbMcpServerMapper serverMapper;
    private final KbMcpEnvMapper envMapper;
    private final PasswordCodec passwordCodec;
    private final KnowledgeMcpProperties properties;
    private final McpTransportFactory transportFactory;

    private final ConcurrentHashMap<Long, CachedClient> cache = new ConcurrentHashMap<>();
    private final AtomicInteger stdioProcessCount = new AtomicInteger(0);

    public McpClientManager(KbMcpServerMapper serverMapper,
                            KbMcpEnvMapper envMapper,
                            PasswordCodec passwordCodec,
                            KnowledgeMcpProperties properties,
                            McpTransportFactory transportFactory) {
        this.serverMapper = serverMapper;
        this.envMapper = envMapper;
        this.passwordCodec = passwordCodec;
        this.properties = properties;
        this.transportFactory = transportFactory;
    }

    /**
     * 获取（或创建）指定 MCP 的同步客户端。
     *
     * @param mcpId MCP 主键
     * @return 已初始化的客户端
     */
    public McpSyncClient getClient(Long mcpId) {
        evictExpired();
        CachedClient cached = cache.get(mcpId);
        if (cached != null && !cached.isExpired(properties.getClientCacheTtlSeconds())) {
            return cached.client();
        }
        synchronized (getLock(mcpId)) {
            cached = cache.get(mcpId);
            if (cached != null && !cached.isExpired(properties.getClientCacheTtlSeconds())) {
                return cached.client();
            }
            if (cached != null) {
                closeQuietly(cached);
                cache.remove(mcpId);
            }
            McpResolvedConfig config = resolveConfig(mcpId);
            boolean stdio = McpTransport.STDIO.equals(config.getTransport());
            if (stdio) {
                int max = properties.getMaxStdioProcesses();
                if (stdioProcessCount.get() >= max) {
                    throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                        "STDIO 子进程数已达上限 " + max);
                }
            }
            io.modelcontextprotocol.spec.McpClientTransport transport = transportFactory.createTransport(config);
            McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofMillis(config.getRequestTimeoutMs()))
                .build();
            client.initialize();
            CachedClient entry = new CachedClient(client, stdio, Instant.now());
            if (stdio) {
                stdioProcessCount.incrementAndGet();
            }
            cache.put(mcpId, entry);
            return client;
        }
    }

    /**
     * 解析运行时配置（不创建客户端）。
     *
     * @param mcpId MCP 主键
     * @return 解析后的配置
     */
    public McpResolvedConfig resolveConfig(Long mcpId) {
        KbMcpServer server = loadServer(mcpId);
        List<KbMcpEnv> envRows = envMapper.selectList(
            Wrappers.<KbMcpEnv>lambdaQuery()
                .eq(KbMcpEnv::getMcpId, mcpId)
                .eq(KbMcpEnv::getDeleted, KnowledgeConstants.NOT_DELETED)
                .orderByAsc(KbMcpEnv::getSortOrder)
        );
        Map<String, String> env = new LinkedHashMap<>();
        for (KbMcpEnv row : envRows) {
            String plain = McpSecretSupport.resolvePlainValue(passwordCodec, row.getValueType(), row.getEnvValue());
            if (plain == null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                    "环境变量未设置: " + row.getEnvKey());
            }
            env.put(row.getEnvKey(), plain);
        }
        List<String> args = new ArrayList<>();
        if (StrUtil.isNotBlank(server.getArgsJson())) {
            args.addAll(JSONUtil.parseArray(server.getArgsJson()).toList(String.class));
        }
        Map<String, String> headers;
        try {
            headers = McpHeaderSupport.resolveHeaders(passwordCodec, server.getHeadersJson());
        } catch (IllegalStateException ex) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, ex.getMessage());
        }
        int timeout = server.getRequestTimeoutMs() == null ? 30_000 : server.getRequestTimeoutMs();
        return McpResolvedConfig.builder()
            .mcpId(server.getMcpId())
            .code(server.getCode())
            .transport(server.getTransport())
            .command(server.getCommand())
            .args(args)
            .url(server.getUrl())
            .headers(headers)
            .env(env)
            .requestTimeoutMs(timeout)
            .build();
    }

    /**
     * 驱逐指定 MCP 的缓存客户端。
     *
     * @param mcpId MCP 主键
     */
    public void evict(Long mcpId) {
        CachedClient cached = cache.remove(mcpId);
        if (cached != null) {
            closeQuietly(cached);
        }
    }

    /**
     * 驱逐全部缓存。
     */
    public void evictAll() {
        cache.keySet().forEach(this::evict);
    }

    @PreDestroy
    public void destroy() {
        evictAll();
    }

    private KbMcpServer loadServer(Long mcpId) {
        if (mcpId == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "MCP ID 不能为空");
        }
        KbMcpServer server = serverMapper.selectById(mcpId);
        if (server == null || KnowledgeConstants.DELETED == server.getDeleted()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "MCP 配置不存在或已删除");
        }
        return server;
    }

    private void evictExpired() {
        int ttl = properties.getClientCacheTtlSeconds();
        if (ttl <= 0) {
            return;
        }
        for (Map.Entry<Long, CachedClient> entry : cache.entrySet()) {
            if (entry.getValue().isExpired(ttl)) {
                evict(entry.getKey());
            }
        }
    }

    private void closeQuietly(CachedClient cached) {
        try {
            cached.client().closeGracefully();
        } catch (Exception ignored) {
            // 关闭失败不阻断业务
        } finally {
            if (cached.stdio()) {
                stdioProcessCount.updateAndGet(v -> Math.max(0, v - 1));
            }
        }
    }

    private Object getLock(Long mcpId) {
        return ("mcp-lock-" + mcpId).intern();
    }

    private record CachedClient(McpSyncClient client, boolean stdio, Instant createdAt) {

        boolean isExpired(int ttlSeconds) {
            if (ttlSeconds <= 0) {
                return false;
            }
            return createdAt.plusSeconds(ttlSeconds).isBefore(Instant.now());
        }
    }
}
