package io.github.genkidoudou.common.security.firewall.idempotent;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 接口幂等防火墙配置，前缀 {@code qc.security.firewall.idempotent}。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "qc.security.firewall.idempotent")
public class IdempotentProperties {

    /**
     * 默认关闭；显式 {@code true} 时注册拦截与存储。
     */
    private boolean enabled = false;

    /**
     * 全局方法级自动拦截的 HTTP 方法名（如 POST、PUT）；空列表表示仅 {@link Idempotent} 注解生效。
     */
    private List<String> interceptMethods = new ArrayList<>();

    /**
     * Ant 风格路径；命中则不进行幂等。
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * 占位默认存活时间数值（与 {@link #expireTimeUnit} 组合）。
     */
    private long expireTime = 60L;

    /**
     * {@link #expireTime} 的单位，默认秒。
     */
    private TimeUnit expireTimeUnit = TimeUnit.SECONDS;

    /**
     * 存储键全局前缀。
     */
    private String keyPrefix = "qc:idempotent:";

    /**
     * 重复请求时文案回退（i18n 未命中时使用）。
     */
    private String defaultMessage = "重复请求，请稍后再试";

    /**
     * 幂等 token 请求头名。
     */
    private String tokenHeader = "X-Idempotent-Token";

    /**
     * 存储实现选择。
     */
    private IdempotentCacheType cacheType = IdempotentCacheType.AUTO;

    /**
     * 解析实际存储类型；{@code redisAvailable} 表示容器中存在可用的 Redis 连接工厂。
     *
     * @throws IllegalStateException {@link IdempotentCacheType#REDIS} 但无 Redis 时 fail-fast
     */
    public IdempotentCacheType resolveEffectiveCacheType(boolean redisAvailable) {
        IdempotentCacheType t = cacheType == null ? IdempotentCacheType.AUTO : cacheType;
        return switch (t) {
            case AUTO -> redisAvailable ? IdempotentCacheType.REDIS : IdempotentCacheType.CAFFEINE;
            case REDIS -> {
                if (!redisAvailable) {
                    throw new IllegalStateException(
                            "qc.security.firewall.idempotent.cache-type=redis 但未发现 RedisConnectionFactory Bean");
                }
                yield IdempotentCacheType.REDIS;
            }
            case CAFFEINE -> IdempotentCacheType.CAFFEINE;
        };
    }
}
