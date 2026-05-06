package io.github.genkidoudou.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class DynamicTtlRedisCacheManagerIT {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private LettuceConnectionFactory newFactory() {
        RedisStandaloneConfiguration c =
                new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379));
        LettuceConnectionFactory f = new LettuceConnectionFactory(c);
        f.afterPropertiesSet();
        return f;
    }

    @Test
    void shortTtlExpires() throws Exception {
        LettuceConnectionFactory factory = newFactory();
        try {
            CacheManager mgr =
                    new TransactionAwareCacheManagerProxy(new DynamicTtlRedisCacheManager(factory, new ObjectMapper()));
            Cache cache = mgr.getCache("a#1");
            assertThat(cache).isNotNull();
            cache.put("k", "v");
            assertThat(cache.get("k")).isNotNull();
            Thread.sleep(1100);
            assertThat(cache.get("k")).isNull();
        } finally {
            factory.destroy();
        }
    }

    @Test
    void mapRoundTripUsesJacksonSerializer() {
        LettuceConnectionFactory factory = newFactory();
        try {
            CacheManager mgr =
                    new TransactionAwareCacheManagerProxy(new DynamicTtlRedisCacheManager(factory, new ObjectMapper()));
            Cache cache = mgr.getCache("json#3600");
            cache.put("row", Map.of("hello", "world"));
            var wrapper = cache.get("row");
            assertThat(wrapper).isNotNull();
            Object body = wrapper.get();
            assertThat(body).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) body;
            assertThat(map.get("hello")).isEqualTo("world");
        } finally {
            factory.destroy();
        }
    }

    @Test
    void nullIsNotCachedAndPutThrowsOrEvicts() {
        LettuceConnectionFactory factory = newFactory();
        try {
            try (RedisConnection flushConn = factory.getConnection()) {
                flushConn.serverCommands().flushDb();
            }
            CacheManager mgr =
                    new TransactionAwareCacheManagerProxy(new DynamicTtlRedisCacheManager(factory, new ObjectMapper()));
            Cache cache = mgr.getCache("nulltest#60");
            assertThatThrownBy(() -> cache.put("nk", null)).isInstanceOf(InvalidDataAccessApiUsageException.class);
            try (RedisConnection conn = factory.getConnection()) {
                assertThat(conn.serverCommands().dbSize()).isZero();
            }
        } finally {
            factory.destroy();
        }
    }
}
