package io.github.genkidoudou.common.cache;

import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.lang.Nullable;

import java.time.Duration;

/**
 * 自定义 Redis 缓存管理器
 * 支持通过 cacheName#ttl 格式动态设置缓存过期时间
 * 
 * 使用示例：
 * @Cacheable(cacheNames = "userCache#3600", key = "#id")
 * 表示缓存名为 userCache，过期时间为 3600 秒（1小时）
 * 
 * @author QuickBoot
 * @date 2026-03-01
 */
public class CustomRedisCacheManager extends RedisCacheManager {

    /**
     * 默认缓存过期时间（秒）
     */
    private static final long DEFAULT_EXPIRE_SECONDS = 3600L;

    /**
     * 缓存名称和过期时间的分隔符
     */
    private static final String SEPARATOR = "#";

    /**
     * Redis 缓存写入器
     */
    private final RedisCacheWriter cacheWriter;

    /**
     * 默认缓存配置
     */
    private final RedisCacheConfiguration defaultCacheConfig;

    /**
     * 构造函数
     * 
     * @param cacheWriter Redis 缓存写入器
     * @param defaultCacheConfiguration 默认缓存配置
     */
    public CustomRedisCacheManager(RedisCacheWriter cacheWriter, 
                                   RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
        this.cacheWriter = cacheWriter;
        this.defaultCacheConfig = defaultCacheConfiguration;
    }

    /**
     * 创建 Redis 缓存实例
     * 解析 cacheName 中的过期时间配置
     * 
     * @param name 缓存名称，格式：cacheName 或 cacheName#ttl
     * @param cacheConfig 缓存配置
     * @return RedisCache 实例
     */
    @Override
    protected RedisCache createRedisCache(String name, @Nullable RedisCacheConfiguration cacheConfig) {
        // 解析缓存名称和过期时间
        String cacheName = name;
        long expireSeconds = DEFAULT_EXPIRE_SECONDS;

        // 如果包含分隔符，则解析过期时间
        if (name.contains(SEPARATOR)) {
            String[] parts = name.split(SEPARATOR);
            cacheName = parts[0];
            
            // 解析过期时间（秒）
            if (parts.length > 1) {
                try {
                    expireSeconds = Long.parseLong(parts[1]);
                } catch (NumberFormatException e) {
                    // 解析失败，使用默认过期时间
                    expireSeconds = DEFAULT_EXPIRE_SECONDS;
                }
            }
        }

        // 使用解析出的过期时间创建新的缓存配置
        RedisCacheConfiguration config = (cacheConfig != null ? cacheConfig : defaultCacheConfig)
                .entryTtl(Duration.ofSeconds(expireSeconds));

        // 创建并返回 RedisCache 实例
        return super.createRedisCache(cacheName, config);
    }

    /**
     * 获取缓存实例
     * 如果缓存不存在，则创建新的缓存
     *
     * @param name 缓存名称
     * @return Cache 实例
     */
    @Override
    @Nullable
    public Cache getCache(String name) {
        // 先尝试从已有缓存中获取
        Cache cache = super.getCache(name);

        // 如果缓存不存在且允许动态创建，则创建新的缓存
        if (cache == null) {
            cache = createRedisCache(name, defaultCacheConfig);
        }

        return cache;
    }
}
