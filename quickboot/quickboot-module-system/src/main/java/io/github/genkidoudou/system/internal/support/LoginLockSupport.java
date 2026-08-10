package io.github.genkidoudou.system.internal.support;

import io.github.genkidoudou.system.internal.config.LoginProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 登录失败计数与临时锁定（Redis）。
 */
@Component
@RequiredArgsConstructor
public class LoginLockSupport {

  private static final String FAIL_KEY = "login:fail:";
  private static final String LOCK_KEY = "login:lock:";

  private final StringRedisTemplate stringRedisTemplate;
  private final LoginProperties loginProperties;

  public boolean isLocked(String username) {
    Boolean has = stringRedisTemplate.hasKey(LOCK_KEY + username);
    return Boolean.TRUE.equals(has);
  }

  /**
   * @return 锁定剩余秒数；未锁定返回 0
   */
  public long lockTtlSeconds(String username) {
    Long ttl = stringRedisTemplate.getExpire(LOCK_KEY + username, TimeUnit.SECONDS);
    return ttl == null || ttl < 0 ? 0 : ttl;
  }

  /**
   * 记录一次凭证失败；达到阈值则锁定并清除计数。
   *
   * @return 本次失败后是否已进入锁定
   */
  public boolean recordFailure(String username) {
    String failKey = FAIL_KEY + username;
    Long count = stringRedisTemplate.opsForValue().increment(failKey);
    if (count == null) {
      count = 1L;
    }
    Duration window = Duration.ofMinutes(Math.max(1, loginProperties.getLockMinutes()));
    if (count == 1L) {
      stringRedisTemplate.expire(failKey, window);
    }
    if (count >= loginProperties.getMaxRetry()) {
      stringRedisTemplate.opsForValue().set(LOCK_KEY + username, "1", window);
      stringRedisTemplate.delete(failKey);
      return true;
    }
    return false;
  }

  public void clear(String username) {
    stringRedisTemplate.delete(FAIL_KEY + username);
    stringRedisTemplate.delete(LOCK_KEY + username);
  }
}
