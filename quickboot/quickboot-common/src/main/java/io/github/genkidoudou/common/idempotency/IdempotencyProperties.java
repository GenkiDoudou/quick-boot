package io.github.genkidoudou.common.idempotency;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 幂等组件配置，前缀 {@code qc.idempotency}。
 */
@Data
@ConfigurationProperties(prefix = "qc.idempotency")
public class IdempotencyProperties {

  /** 是否启用幂等切面。 */
  private boolean enabled = true;

  /** 默认幂等键占用 TTL（秒）。 */
  private int ttlSeconds = 300;

  /** 读取客户端幂等键的 HTTP Header 名。 */
  private String headerName = IdempotencyKeys.HEADER_NAME;

  /** Redis / 内存键前缀。 */
  private String keyPrefix = "qc:idempotency:";

  /**
   * 存储实现：{@code redis} 使用 {@link org.springframework.data.redis.core.StringRedisTemplate}；
   * {@code local} 使用进程内 Map（适合单实例开发或无 Redis 场景）。
   */
  private String store = "redis";
}
