package io.github.genkidoudou.common.security.firewall.idempotent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotentPropertiesTest {

    @Test
    void autoUsesRedisWhenAvailable() {
        IdempotentProperties p = new IdempotentProperties();
        p.setCacheType(IdempotentCacheType.AUTO);
        assertThat(p.resolveEffectiveCacheType(true)).isEqualTo(IdempotentCacheType.REDIS);
        assertThat(p.resolveEffectiveCacheType(false)).isEqualTo(IdempotentCacheType.CAFFEINE);
    }

    @Test
    void redisWithoutFactoryFailsFast() {
        IdempotentProperties p = new IdempotentProperties();
        p.setCacheType(IdempotentCacheType.REDIS);
        assertThatThrownBy(() -> p.resolveEffectiveCacheType(false))
                .isInstanceOf(IllegalStateException.class);
    }
}
