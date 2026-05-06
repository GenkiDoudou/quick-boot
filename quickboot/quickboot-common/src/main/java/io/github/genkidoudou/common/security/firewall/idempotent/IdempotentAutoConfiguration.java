package io.github.genkidoudou.common.security.firewall.idempotent;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 接口幂等（TOKEN-only）自动配置；{@code enabled=true} 时生效。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(IdempotentProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.idempotent", name = "enabled", havingValue = "true")
public class IdempotentAutoConfiguration {

    @Bean
    public IdempotentStore idempotentStore(IdempotentProperties properties,
                                           ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider,
                                           ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider) {
        boolean redisAvailable = redisConnectionFactoryProvider.getIfAvailable() != null;
        IdempotentCacheType mode = properties.resolveEffectiveCacheType(redisAvailable);
        if (mode == IdempotentCacheType.REDIS) {
            RedisConnectionFactory factory = redisConnectionFactoryProvider.getIfAvailable();
            StringRedisTemplate tpl = stringRedisTemplateProvider.getIfAvailable();
            if (tpl == null) {
                tpl = new StringRedisTemplate(factory);
                tpl.afterPropertiesSet();
            }
            return new RedisIdempotentStore(tpl);
        }
        return new CaffeineIdempotentStore();
    }

    @Bean
    public IdempotentInterceptor idempotentInterceptor(IdempotentProperties properties, IdempotentStore store) {
        return new IdempotentInterceptor(properties, store);
    }

    @Bean
    public IdempotentWebConfiguration idempotentWebConfiguration(IdempotentInterceptor interceptor) {
        return new IdempotentWebConfiguration(interceptor);
    }

    @Bean
    public IdempotentExceptionHandler idempotentExceptionHandler() {
        return new IdempotentExceptionHandler();
    }
}
