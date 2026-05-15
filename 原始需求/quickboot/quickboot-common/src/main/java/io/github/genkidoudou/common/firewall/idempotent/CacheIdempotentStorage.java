package io.github.genkidoudou.common.firewall.idempotent;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Spring Cache 的幂等存储实现
 * 支持 Redis 和 Caffeine
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
public class CacheIdempotentStorage implements IdempotentStorage {

    private static final String CACHE_NAME = "idempotent";
    private static final String LOCK_VALUE = "1";

    private final CacheManager cacheManager;

    public CacheIdempotentStorage(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public boolean setIfAbsent(String key, String value, long expireTime, TimeUnit timeUnit) {
        Cache cache = getCache();
        
        // 检查键是否已存在
        if (cache.get(key) != null) {
            return false;
        }
        
        // 设置键
        cache.put(key, value);
        return true;
    }

    @Override
    public void delete(String key) {
        Cache cache = getCache();
        cache.evict(key);
    }

    @Override
    public boolean exists(String key) {
        Cache cache = getCache();
        return cache.get(key) != null;
    }

    private Cache getCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException("缓存 '" + CACHE_NAME + "' 不存在，请检查缓存配置");
        }
        return cache;
    }
}
