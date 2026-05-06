package io.github.genkidoudou.common.security.firewall.idempotent;

/**
 * 幂等存储后端：与配置 {@code qc.security.firewall.idempotent.cache-type} 对应。
 */
public enum IdempotentCacheType {

    /** 存在 {@link org.springframework.data.redis.connection.RedisConnectionFactory} 时用 Redis，否则用进程内存储。 */
    AUTO,
    REDIS,
    CAFFEINE
}
