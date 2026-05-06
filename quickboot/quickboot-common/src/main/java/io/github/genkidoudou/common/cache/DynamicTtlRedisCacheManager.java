package io.github.genkidoudou.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Collections;

/**
 * 基于 RedisCacheManager，按注解完整缓存名懒建 {@link RedisCache}；TTL 来自 {@code cacheName#ttlSeconds}。
 * <p>
 * Redis 分区名使用注解原始字符串（含 {@code #ttl}），避免同一逻辑名不同 TTL 时 key 冲突；
 * 业务侧 {@link org.springframework.cache.annotation.CacheEvict} 必须与 {@link org.springframework.cache.annotation.Cacheable} 使用相同的 cacheNames。
 */
public class DynamicTtlRedisCacheManager extends RedisCacheManager {

    private final ObjectMapper objectMapper;

    public DynamicTtlRedisCacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        super(
                RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory),
                defaultConfiguration(objectMapper),
                Collections.emptyMap(),
                true);
        this.objectMapper = objectMapper.copy();
    }

    private static RedisCacheConfiguration defaultConfiguration(ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper.copy());
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(QuickbootCacheDefaults.DEFAULT_TTL_SECONDS))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer.UTF_8))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();
    }

    @Override
    protected RedisCache createRedisCache(String name, @Nullable RedisCacheConfiguration cacheConfig) {
        CacheNameTtl parsed = CacheNameTtlParser.parse(name);
        RedisCacheConfiguration base =
                cacheConfig != null ? cacheConfig : defaultConfiguration(objectMapper);
        RedisCacheConfiguration withTtl = base.entryTtl(Duration.ofSeconds(parsed.ttlSeconds()));
        return super.createRedisCache(name, withTtl);
    }
}
