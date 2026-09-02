package io.github.genkidoudou.common.idempotency;

/**
 * {@link Idempotent} 服务端幂等键的作用域。
 */
public enum IdempotencyScope {

  /** 按登录用户（或匿名）隔离；推荐用于管理端写操作。 */
  USER,

  /** 仅使用客户端幂等键 + 可选 URI，不按用户隔离。 */
  GLOBAL
}
