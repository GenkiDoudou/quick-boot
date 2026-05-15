package io.github.genkidoudou.common.firewall.idempotent;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * 幂等自动配置类
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
@Configuration
@EnableConfigurationProperties(IdempotentProperties.class)
@ConditionalOnProperty(prefix = "qc.security.firewall.idempotent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IdempotentConfiguration {

    /**
     * 幂等存储
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentStorage idempotentStorage(CacheManager cacheManager) {
        return new CacheIdempotentStorage(cacheManager);
    }

    /**
     * 默认幂等键生成器
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentKeyGenerator idempotentKeyGenerator(IdempotentProperties properties) {
        return new DefaultIdempotentKeyGenerator(properties);
    }

    /**
     * 路径匹配器
     */
    @Bean
    @ConditionalOnMissingBean
    public PathMatcher pathMatcher() {
        return new AntPathMatcher();
    }

    /**
     * 幂等切面
     */
    @Bean
    public IdempotentAspect idempotentAspect(IdempotentProperties properties,
                                            IdempotentStorage storage,
                                            IdempotentKeyGenerator keyGenerator,
                                            PathMatcher pathMatcher) {
        return new IdempotentAspect(properties, storage, keyGenerator, pathMatcher);
    }
}
