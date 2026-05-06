package io.github.genkidoudou.common.security.firewall.idempotent;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * 基于 Redis {@code SET NX EX} 的幂等存储（多实例一致）。
 */
public class RedisIdempotentStore implements IdempotentStore {

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotentStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        long sec = Math.max(1, ttl.getSeconds());
        if (ttl.toMillis() > 0 && sec == 0) {
            sec = 1;
        }
        try {
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(sec));
            return Boolean.TRUE.equals(ok);
        } catch (DataAccessException e) {
            throw new IllegalStateException("Redis 幂等占位失败", e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException ignored) {
        }
    }
}
