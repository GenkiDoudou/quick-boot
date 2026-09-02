package io.github.genkidoudou.common.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis {@code SET NX EX} 的分布式幂等键存储。
 */
@RequiredArgsConstructor
public final class RedisIdempotencyStore implements IdempotencyStore {

  private static final String PLACEHOLDER = "1";

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public boolean tryAcquire(String key, Duration ttl) {
    long seconds = Math.max(1L, ttl.getSeconds());
    Boolean acquired = stringRedisTemplate.opsForValue()
        .setIfAbsent(key, PLACEHOLDER, seconds, TimeUnit.SECONDS);
    return Boolean.TRUE.equals(acquired);
  }

  @Override
  public void release(String key) {
    stringRedisTemplate.delete(key);
  }
}
