package io.github.genkidoudou.common.security.firewall.idempotent;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CaffeineIdempotentStoreTest {

    @Test
    void secondSetIfAbsentFailsWithinDeadline() {
        CaffeineIdempotentStore store = new CaffeineIdempotentStore();
        String k = "k1";
        assertThat(store.setIfAbsent(k, Duration.ofSeconds(60))).isTrue();
        assertThat(store.setIfAbsent(k, Duration.ofSeconds(60))).isFalse();
    }

    @Test
    void deleteAllowsReacquire() {
        CaffeineIdempotentStore store = new CaffeineIdempotentStore();
        String k = "k2";
        assertThat(store.setIfAbsent(k, Duration.ofHours(1))).isTrue();
        store.delete(k);
        assertThat(store.setIfAbsent(k, Duration.ofSeconds(1))).isTrue();
    }
}
