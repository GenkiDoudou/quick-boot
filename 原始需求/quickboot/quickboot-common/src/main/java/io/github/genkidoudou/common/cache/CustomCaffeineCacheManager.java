package io.github.genkidoudou.common.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 自定义 Caffeine 缓存管理器
 * 支持通过 cacheName#ttl 格式动态设置缓存过期时间
 * <p>
 * 使用示例：
 *
 * @author QuickBoot
 * @Cacheable(cacheNames = "userCache#3600", key = "#id")
 * 表示缓存名为 userCache，过期时间为 3600 秒（1小时）
 * @date 2026-03-01
 */
public class CustomCaffeineCacheManager extends CaffeineCacheManager {

    /**
     * 默认缓存过期时间（秒）
     */
    private static final long DEFAULT_EXPIRE_SECONDS = 3600L;

    /**
     * 缓存名称和过期时间的分隔符
     */
    private static final String SEPARATOR = "#";

    /**
     * 默认最大缓存条目数
     */
    private static final long DEFAULT_MAXIMUM_SIZE = 10000L;

    /**
     * 缓存实例映射表，用于缓存已创建的 Cache 实例
     */
    private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();

    /**
     * 创建缓存实例
     * 解析 cacheName 中的过期时间配置
     *
     * @param name 缓存名称，格式：cacheName 或 cacheName#ttl
     * @return Cache 实例
     */
    @Override
    @Nullable
    protected Cache createCaffeineCache(String name) {
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

        // 创建带有自定义过期时间的 Caffeine 缓存
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .maximumSize(DEFAULT_MAXIMUM_SIZE) // 最大缓存条目数
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS) // 写入后过期时间
                .recordStats(); // 开启统计

        // 返回 CaffeineCache 实例
        return new CaffeineCache(cacheName, caffeineBuilder.build());
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
            cache = createCaffeineCache(name);
        }

        return cache;
    }
}
