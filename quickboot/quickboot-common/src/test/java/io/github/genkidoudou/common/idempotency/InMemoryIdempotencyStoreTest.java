package io.github.genkidoudou.common.idempotency;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryIdempotencyStoreTest {

  private final InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();

  @Test
  void acquireOnceThenReject() {
    String key = "test-key";
    assertTrue(store.tryAcquire(key, Duration.ofSeconds(60)));
    assertFalse(store.tryAcquire(key, Duration.ofSeconds(60)));
  }

  @Test
  void releaseAllowsRetry() {
    String key = "retry-key";
    assertTrue(store.tryAcquire(key, Duration.ofSeconds(60)));
    store.release(key);
    assertTrue(store.tryAcquire(key, Duration.ofSeconds(60)));
  }

  @Test
  void expiredKeyCanReacquire() throws InterruptedException {
    String key = "expire-key";
    assertTrue(store.tryAcquire(key, Duration.ofMillis(50)));
    Thread.sleep(60);
    assertTrue(store.tryAcquire(key, Duration.ofSeconds(60)));
  }
}
