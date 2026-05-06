package io.github.genkidoudou.common.security.firewall.password;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link DefaultPasswordCodec} 行为与 OpenSpec「firewall-password-codec」对齐的单测。
 */
class PasswordCodecTest {

    private static final String KEY_A = "0123456789abcdef0123456789abcdef";
    private static final String KEY_B = "fedcba9876543210fedcba9876543210";

    @Test
    void bcrypt_encrypt_then_matches() {
        DefaultPasswordCodec c = codecWithSm4Keys();
        String enc = c.encrypt("hello", "bcrypt");
        assertThat(enc).startsWith("{bcrypt}");
        assertThat(c.matches("hello", enc)).isTrue();
        assertThat(c.matches("wrong", enc)).isFalse();
    }

    @Test
    void bcrypt_legacy_no_prefix_matches() {
        DefaultPasswordCodec c = codecWithSm4Keys();
        String hash = BCrypt.hashpw("legacy", BCrypt.gensalt());
        assertThat(hash).doesNotStartWith("{");
        assertThat(c.matches("legacy", hash)).isTrue();
        assertThat(c.matches("x", hash)).isFalse();
    }

    @Test
    void bcrypt_decrypt_fails() {
        DefaultPasswordCodec c = codecWithSm4Keys();
        String enc = c.encrypt("x", "bcrypt");
        assertThatThrownBy(() -> c.decrypt(enc)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sm4_roundTrip_matches_decrypt_multiKey() {
        DefaultPasswordCodec c = codecWithSm4Keys();
        String encA = c.encrypt("token-a", "sm4:keyA");
        assertThat(encA).startsWith("{sm4:keyA}");
        assertThat(c.matches("token-a", encA)).isTrue();
        assertThat(c.decrypt(encA)).isEqualTo("token-a");

        String encB = c.encrypt("token-b", "sm4:keyB");
        assertThat(encB).startsWith("{sm4:keyB}");
        assertThat(c.matches("token-b", encB)).isTrue();
        assertThat(c.matches("token-a", encB)).isFalse();
    }

    @Test
    void sm4_defaultKeyId_encrypt() {
        DefaultPasswordCodec c = codecWithSm4Keys();
        String enc = c.encrypt("d", "sm4");
        assertThat(enc).startsWith("{sm4:keyA}");
        assertThat(c.matches("d", enc)).isTrue();
    }

    @Test
    void sm4_missingKey_throws() {
        DefaultPasswordCodec c = codecWithSm4Keys();
        assertThatThrownBy(() -> c.encrypt("x", "sm4:missing"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("未注册的 SM4 keyId");
    }

    @Test
    void sm4_wrongKeyId_decrypt_fails_or_matches_false() {
        DefaultPasswordCodec c = codecWithSm4Keys();
        String encA = c.encrypt("secret", "sm4:keyA");
        String tampered = "{sm4:keyB}" + encA.substring(encA.indexOf('}') + 1);
        assertThat(c.matches("secret", tampered)).isFalse();
        assertThatThrownBy(() -> c.decrypt(tampered)).isInstanceOf(RuntimeException.class);
    }

    private static DefaultPasswordCodec codecWithSm4Keys() {
        DefaultPasswordCodec c = new DefaultPasswordCodec();
        Properties p = new Properties();
        p.setProperty("sm4.defaultKeyId", "keyA");
        p.setProperty("sm4.keys.keyA", KEY_A);
        p.setProperty("sm4.keys.keyB", KEY_B);
        c.setProperties(p);
        return c;
    }
}
