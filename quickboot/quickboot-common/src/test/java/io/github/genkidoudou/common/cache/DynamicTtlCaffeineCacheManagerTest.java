package io.github.genkidoudou.common.cache;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicTtlCaffeineCacheManagerTest {

    @Test
    void cacheWithOneSecondTtlExpires() throws InterruptedException {
        CacheManager mgr = new DynamicTtlCaffeineCacheManager();
        Cache cache = mgr.getCache("a#1");
        assertThat(cache).isNotNull();
        cache.put("k", "v");
        assertThat(cache.get("k")).isNotNull();
        Thread.sleep(1100);
        assertThat(cache.get("k")).isNull();
    }

    @Test
    void cacheWithoutSuffixUsesDefaultTtlFromParser() {
        CacheNameTtl parsed = CacheNameTtlParser.parse("plain");
        assertThat(parsed.ttlSeconds()).isEqualTo(QuickbootCacheDefaults.DEFAULT_TTL_SECONDS);
        CacheManager mgr = new DynamicTtlCaffeineCacheManager();
        Cache cache = mgr.getCache("plain");
        assertThat(cache).isNotNull();
        cache.put("x", "y");
        assertThat(cache.get("x")).isNotNull();
    }
}
