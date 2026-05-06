package io.github.genkidoudou.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;

class QuickbootCacheAutoConfigurationTest {

    @Test
    void caffeineManagerIsTransactionAwareProxy() {
        QuickbootCacheAutoConfiguration cfg = new QuickbootCacheAutoConfiguration();
        CacheManager cm = cfg.quickbootCaffeineCacheManager();
        assertThat(cm).isInstanceOf(TransactionAwareCacheManagerProxy.class);
    }

    @Test
    void redisManagerIsTransactionAwareProxy() {
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration("127.0.0.1", 56379);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone);
        factory.afterPropertiesSet();
        try {
            QuickbootCacheAutoConfiguration cfg = new QuickbootCacheAutoConfiguration();
            CacheManager cm = cfg.quickbootRedisCacheManager(factory, new ObjectMapper());
            assertThat(cm).isInstanceOf(TransactionAwareCacheManagerProxy.class);
        } finally {
            factory.destroy();
        }
    }
}
