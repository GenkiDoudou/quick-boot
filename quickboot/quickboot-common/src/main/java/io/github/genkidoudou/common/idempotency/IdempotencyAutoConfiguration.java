package io.github.genkidoudou.common.idempotency;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 注册幂等切面与 {@link IdempotencyStore} 实现（Redis 或进程内）。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(IdempotencyProperties.class)
@ConditionalOnProperty(prefix = "qc.idempotency", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IdempotencyAutoConfiguration {

  /**
   * Redis 分布式幂等存储（默认 {@code qc.idempotency.store=redis} 且存在 {@link StringRedisTemplate}）。
   */
  @Bean
  @ConditionalOnMissingBean(IdempotencyStore.class)
  @ConditionalOnProperty(prefix = "qc.idempotency", name = "store", havingValue = "redis", matchIfMissing = true)
  @ConditionalOnBean(StringRedisTemplate.class)
  public IdempotencyStore redisIdempotencyStore(StringRedisTemplate stringRedisTemplate) {
    return new RedisIdempotencyStore(stringRedisTemplate);
  }

  /**
   * 显式 {@code store=local} 时的进程内存储。
   */
  @Bean
  @ConditionalOnMissingBean(IdempotencyStore.class)
  @ConditionalOnProperty(prefix = "qc.idempotency", name = "store", havingValue = "local")
  public IdempotencyStore localIdempotencyStore() {
    return new InMemoryIdempotencyStore();
  }

  /**
   * 无 Redis 或未匹配上述条件时的进程内回退存储。
   */
  @Bean
  @ConditionalOnMissingBean(IdempotencyStore.class)
  public IdempotencyStore fallbackIdempotencyStore() {
    return new InMemoryIdempotencyStore();
  }

  /**
   * 幂等切面 Bean。
   */
  @Bean
  @ConditionalOnMissingBean
  public IdempotentAspect idempotentAspect(IdempotencyStore idempotencyStore, IdempotencyProperties properties) {
    return new IdempotentAspect(idempotencyStore, properties);
  }
}
