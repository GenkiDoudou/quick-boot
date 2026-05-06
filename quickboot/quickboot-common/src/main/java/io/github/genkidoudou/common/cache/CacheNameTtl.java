package io.github.genkidoudou.common.cache;

/**
 * 从注解 {@code cacheNames} 解析得到的逻辑分区名与 TTL。
 *
 * @param logicalName {@code #ttl} 之前的分区名；非法后缀时与原始入参一致（整个字符串作为分区名）
 * @param ttlSeconds  过期秒数，至少为 1（无效时使用默认值）
 */
public record CacheNameTtl(String logicalName, int ttlSeconds) {
}
