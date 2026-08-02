package io.github.genkidoudou.common.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.AbstractCacheManager;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

/**
 * 基于 Caffeine，按注解完整缓存名懒建实例；TTL 来自 {@code cacheName#ttlSeconds}。
 * <p>
 * 每个不同的注解字符串对应独立的 Caffeine 实例，单实例上限 {@link CacheDefaults#CAFFEINE_MAX_ENTRIES_PER_CACHE}。
 */
public class DynamicTtlCaffeineCacheManager extends AbstractCacheManager {

  @Override
  protected Collection<? extends Cache> loadCaches() {
    return Collections.emptyList();
  }

  @Override
  protected Cache getMissingCache(String name) {
    CacheNameTtl parsed = CacheNameTtlParser.parse(name);
    com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
      Caffeine.newBuilder()
        .maximumSize(CacheDefaults.CAFFEINE_MAX_ENTRIES_PER_CACHE)
        .expireAfterWrite(Duration.ofSeconds(parsed.ttlSeconds()))
        .build();
    return new CaffeineCache(name, nativeCache);
  }
}
