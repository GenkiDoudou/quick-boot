package io.github.genkidoudou.common.idempotency;

import java.time.Duration;

/**
 * 幂等键占用存储 SPI：{@link #tryAcquire} 成功表示本请求获得执行权；
 * 业务失败时可 {@link #release} 以便客户端重试。
 */
public interface IdempotencyStore {

  /**
   * 尝试占用幂等键。
   *
   * @param key 服务端规范化后的完整键
   * @param ttl 占用时长
   * @return {@code true} 首次占用成功；{@code false} 键仍被有效占用
   */
  boolean tryAcquire(String key, Duration ttl);

  /**
   * 释放幂等键占用（通常在业务方法抛异常时调用）。
   *
   * @param key 与 {@link #tryAcquire} 相同的键
   */
  void release(String key);
}
