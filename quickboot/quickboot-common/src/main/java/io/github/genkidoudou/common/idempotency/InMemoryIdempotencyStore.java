package io.github.genkidoudou.common.idempotency;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内幂等键存储：适合单实例开发或集成测试，不跨 JVM 共享。
 */
public final class InMemoryIdempotencyStore implements IdempotencyStore {

  /** 键 → 过期时间戳（毫秒）。 */
  private final ConcurrentHashMap<String, Long> expiresAt = new ConcurrentHashMap<>();

  @Override
  public boolean tryAcquire(String key, Duration ttl) {
    long now = System.currentTimeMillis();
    long newExpire = now + ttl.toMillis();
    Long existing = expiresAt.putIfAbsent(key, newExpire);
    if (existing == null) {
      return true;
    }
    if (existing <= now) {
      return expiresAt.replace(key, existing, newExpire);
    }
    return false;
  }

  @Override
  public void release(String key) {
    expiresAt.remove(key);
  }
}
