package io.github.genkidoudou.web.system.oauthclient.service;

import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import io.github.genkidoudou.web.system.oauthclient.mapper.SysOauthClientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * OAuth 客户端只读查询（带 Spring Cache），供签名校验等热路径复用，避免每请求查库。
 */
@Service
@RequiredArgsConstructor
public class OauthClientLookupService {

    /** 客户端实体缓存，TTL 见 cacheNames 后缀（默认 300 秒）。 */
    public static final String CLIENT_CACHE = "qc-oauth-client#300";

    private final SysOauthClientMapper mapper;

    /**
     * 按 clientId 加载客户端（未删除记录；不存在时返回 {@code null}，不写入缓存）。
     *
     * @param clientId 客户端 id
     * @return 实体或 null
     */
    @Cacheable(cacheNames = CLIENT_CACHE, key = "#clientId", unless = "#result == null")
    public SysOauthClient getByClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        return mapper.selectById(clientId.trim());
    }

    /**
     * 客户端配置变更后驱逐缓存。
     *
     * @param clientId 客户端 id
     */
    @CacheEvict(cacheNames = CLIENT_CACHE, key = "#clientId")
    public void evict(String clientId) {
        // 注解驱动驱逐
    }

    /**
     * 批量删除后驱逐。
     *
     * @param clientIds 客户端 id 列表
     */
    public void evictAll(Iterable<String> clientIds) {
        if (clientIds == null) {
            return;
        }
        for (String clientId : clientIds) {
            if (clientId != null && !clientId.isBlank()) {
                evict(clientId.trim());
            }
        }
    }
}
