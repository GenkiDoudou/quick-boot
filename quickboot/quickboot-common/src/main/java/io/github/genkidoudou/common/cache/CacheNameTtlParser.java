package io.github.genkidoudou.common.cache;

/**
 * 解析 {@code cacheName#ttlSeconds}：后缀为正整数秒则生效，否则回退默认 TTL。
 */
public final class CacheNameTtlParser {

  private CacheNameTtlParser() {
  }

  /**
   * @param rawCacheName Spring Cache 传入的缓存名（含可选 {@code #ttl} 后缀）
   */
  public static CacheNameTtl parse(String rawCacheName) {
    if (rawCacheName == null || rawCacheName.isEmpty()) {
      return new CacheNameTtl("", CacheDefaults.DEFAULT_TTL_SECONDS);
    }
    int hash = rawCacheName.lastIndexOf('#');
    if (hash <= 0 || hash == rawCacheName.length() - 1) {
      return new CacheNameTtl(rawCacheName, CacheDefaults.DEFAULT_TTL_SECONDS);
    }
    String suffix = rawCacheName.substring(hash + 1);
    if (!suffix.chars().allMatch(Character::isDigit)) {
      return new CacheNameTtl(rawCacheName, CacheDefaults.DEFAULT_TTL_SECONDS);
    }
    try {
      long ttlLong = Long.parseLong(suffix);
      if (ttlLong <= 0 || ttlLong > Integer.MAX_VALUE) {
        return new CacheNameTtl(rawCacheName, CacheDefaults.DEFAULT_TTL_SECONDS);
      }
      String logical = rawCacheName.substring(0, hash);
      if (logical.isEmpty()) {
        return new CacheNameTtl(rawCacheName, CacheDefaults.DEFAULT_TTL_SECONDS);
      }
      return new CacheNameTtl(logical, (int) ttlLong);
    } catch (NumberFormatException ignored) {
      return new CacheNameTtl(rawCacheName, CacheDefaults.DEFAULT_TTL_SECONDS);
    }
  }
}
