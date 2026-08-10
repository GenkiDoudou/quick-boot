package io.github.genkidoudou.common.desensitization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SensitiveMaskingTest {

  @Test
  void mobile_full() {
    assertEquals("138****5678", SensitiveMasking.mask("13812345678", SensitiveType.MOBILE, ""));
  }

  @Test
  void mobile_short_unchanged() {
    assertEquals("12345", SensitiveMasking.mask("12345", SensitiveType.MOBILE, ""));
  }

  @Test
  void custom_three_four() {
    assertEquals("ABC***GHIJ", SensitiveMasking.mask("ABCDEFGHIJ", SensitiveType.CUSTOM, "3,4"));
  }

  @Test
  void custom_invalid_strategy() {
    assertEquals("ABCDEFGHIJ", SensitiveMasking.mask("ABCDEFGHIJ", SensitiveType.CUSTOM, "oops"));
  }

  @Test
  void name_single_char() {
    assertEquals("张", SensitiveMasking.mask("张", SensitiveType.NAME, ""));
  }

  @Test
  void email_standard() {
    assertEquals("ab***@example.com", SensitiveMasking.mask("abcde@example.com", SensitiveType.EMAIL, ""));
  }

  @Test
  void email_short_local() {
    assertEquals("a@b.com", SensitiveMasking.mask("a@b.com", SensitiveType.EMAIL, ""));
  }

  @Test
  void password_masked() {
    assertEquals("******", SensitiveMasking.mask("secret", SensitiveType.PASSWORD, ""));
  }

  @Test
  void password_empty_unchanged() {
    assertEquals("", SensitiveMasking.mask("", SensitiveType.PASSWORD, ""));
    assertNull(SensitiveMasking.mask(null, SensitiveType.PASSWORD, ""));
  }

  @Test
  void null_blank_passthrough() {
    assertNull(SensitiveMasking.mask(null, SensitiveType.MOBILE, ""));
    assertEquals("", SensitiveMasking.mask("", SensitiveType.NAME, ""));
  }
}
