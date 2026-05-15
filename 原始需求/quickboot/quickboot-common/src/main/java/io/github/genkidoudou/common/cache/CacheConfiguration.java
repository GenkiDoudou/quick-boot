package io.github.genkidoudou.common.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 支持 Caffeine 本地缓存和 Redis 分布式缓存
 * 支持通过 cacheName#ttl 格式动态设置过期时间
 * 
 * @author QuickBoot
 * @date 2026-03-01
 */
@Configuration
public class CacheConfiguration {

    /**
     * 默认缓存过期时间（秒）
     */
    private static final long DEFAULT_EXPIRE_SECONDS = 3600L;

    /**
     * 配置 Caffeine 本地缓存管理器
     * 使用自定义的 CustomCaffeineCacheManager 支持动态过期时间
     * 
     * @return CaffeineCacheManager
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
    public CacheManager caffeineCacheManager() {
        CustomCaffeineCacheManager cacheManager = new CustomCaffeineCacheManager();
        
        // 设置默认的 Caffeine 配置
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000) // 最大缓存条目数
                .expireAfterWrite(DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS) // 默认过期时间
                .recordStats()); // 开启统计
        
        return cacheManager;
    }

    /**
     * 配置 Redis 缓存管理器
     * 使用自定义的 CustomRedisCacheManager 支持动态过期时间
     * 
     * @param connectionFactory Redis 连接工厂
     * @return RedisCacheManager
     */
    @Bean
    @Primary
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // 配置默认的 Redis 缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(DEFAULT_EXPIRE_SECONDS)) // 默认过期时间
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存 null 值

        // 使用自定义的 RedisCacheManager 支持动态过期时间
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .transactionAware() // 支持事务
                .build();
    }
}
