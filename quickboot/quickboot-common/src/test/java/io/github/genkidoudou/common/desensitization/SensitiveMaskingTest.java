package io.github.genkidoudou.common.desensitization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveMaskingTest {

    @Test
    void mobile_full() {
        assertThat(SensitiveMasking.mask("13812345678", SensitiveType.MOBILE, ""))
                .isEqualTo("138****5678");
    }

    @Test
    void mobile_short_unchanged() {
        assertThat(SensitiveMasking.mask("12345", SensitiveType.MOBILE, "")).isEqualTo("12345");
    }

    @Test
    void custom_three_four() {
        assertThat(SensitiveMasking.mask("ABCDEFGHIJ", SensitiveType.CUSTOM, "3,4")).isEqualTo("ABC***GHIJ");
    }

    @Test
    void custom_invalid_strategy() {
        assertThat(SensitiveMasking.mask("ABCDEFGHIJ", SensitiveType.CUSTOM, "oops")).isEqualTo("ABCDEFGHIJ");
    }

    @Test
    void name_single_char() {
        assertThat(SensitiveMasking.mask("张", SensitiveType.NAME, "")).isEqualTo("张");
    }

    @Test
    void email_standard() {
        assertThat(SensitiveMasking.mask("abcde@example.com", SensitiveType.EMAIL, ""))
                .isEqualTo("ab***@example.com");
    }

    @Test
    void email_short_local() {
        assertThat(SensitiveMasking.mask("a@b.com", SensitiveType.EMAIL, "")).isEqualTo("a@b.com");
    }

    @Test
    void password_masked() {
        assertThat(SensitiveMasking.mask("secret", SensitiveType.PASSWORD, "")).isEqualTo("******");
    }

    @Test
    void password_empty_unchanged() {
        assertThat(SensitiveMasking.mask("", SensitiveType.PASSWORD, "")).isEqualTo("");
        assertThat(SensitiveMasking.mask(null, SensitiveType.PASSWORD, "")).isNull();
    }

    @Test
    void null_blank_passthrough() {
        assertThat(SensitiveMasking.mask(null, SensitiveType.MOBILE, "")).isNull();
        assertThat(SensitiveMasking.mask("", SensitiveType.NAME, "")).isEqualTo("");
    }
}
