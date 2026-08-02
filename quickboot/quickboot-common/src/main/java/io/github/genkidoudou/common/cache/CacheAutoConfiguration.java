package io.github.genkidoudou.common.cache;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import tools.jackson.databind.ObjectMapper;

/**
 * quickboot-common 缓存自动配置：按 {@code spring.cache.type} 切换 Caffeine / Redis。
 * <p>
 * 应用模块需自行启用 {@link EnableCaching}（例如在启动类上）；本自动配置仅注册 {@link CacheManager}。
 */
@AutoConfiguration(beforeName = "org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration")
@ConditionalOnClass(CacheManager.class)
public class CacheAutoConfiguration {

  @Bean
  @Primary
  @ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "caffeine", matchIfMissing = true)
  public CacheManager quickbootCaffeineCacheManager() {
    return new TransactionAwareCacheManagerProxy(new DynamicTtlCaffeineCacheManager());
  }

  @Bean
  @Primary
  @ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "redis")
  @ConditionalOnBean({RedisConnectionFactory.class, ObjectMapper.class})
  public CacheManager quickbootRedisCacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
    DynamicTtlRedisCacheManager delegate = new DynamicTtlRedisCacheManager(redisConnectionFactory, objectMapper);
    return new TransactionAwareCacheManagerProxy(delegate);
  }
}
