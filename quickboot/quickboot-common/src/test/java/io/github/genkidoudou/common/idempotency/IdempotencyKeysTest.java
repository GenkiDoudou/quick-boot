package io.github.genkidoudou.common.idempotency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IdempotencyKeysTest {

  @Test
  void normalize_validUuid() {
    assertEquals("550e8400-e29b-41d4-a716-446655440000",
        IdempotencyKeys.normalizeHeader("550e8400-e29b-41d4-a716-446655440000"));
  }

  @Test
  void normalize_trims() {
    assertEquals("abc", IdempotencyKeys.normalizeHeader("  abc  "));
  }

  @Test
  void normalize_blankOrInvalid() {
    assertNull(IdempotencyKeys.normalizeHeader(null));
    assertNull(IdempotencyKeys.normalizeHeader(""));
    assertNull(IdempotencyKeys.normalizeHeader("bad key"));
    assertNull(IdempotencyKeys.normalizeHeader("a".repeat(IdempotencyKeys.MAX_LENGTH + 1)));
  }
}
