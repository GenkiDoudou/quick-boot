package io.github.genkidoudou.common.cache;

/**
 * quickboot-common 缓存模块默认常量（与原始需求 / OpenSpec common-cache 对齐）。
 */
public final class CacheDefaults {

  /**
   * 未解析或非正 TTL 时的默认过期时间（秒）。
   */
  public static final int DEFAULT_TTL_SECONDS = 3600;

  /**
   * Caffeine 每个注解缓存实例的最大条目数（{@code expireAfterWrite} 分区）。
   */
  public static final int CAFFEINE_MAX_ENTRIES_PER_CACHE = 10_000;

  private CacheDefaults() {
  }
}
