package io.github.genkidoudou.common.monitor.operlog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperLogSensitiveMaskerTest {

    @Test
    void masks_password_json_value() {
        String raw = "{\"username\":\"a\",\"password\":\"secret\"}";
        String masked = OperLogSensitiveMasker.mask(raw);
        assertTrue(masked.contains("******"));
        assertFalse(masked.contains("secret"));
    }

    @Test
    void masks_token_query_string() {
        String raw = "username=a&token=abc123&x=1";
        String masked = OperLogSensitiveMasker.mask(raw);
        assertTrue(masked.contains("token=******"));
    }
}
