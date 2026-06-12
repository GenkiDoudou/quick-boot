package io.github.genkidoudou.web.knowledge.mcp.support;

import io.github.genkidoudou.common.exception.WarningException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link McpUrlGuard} SSRF 校验单测。
 */
class McpUrlGuardTest {

    private final McpUrlGuard guard = new McpUrlGuard();

    @Test
    void rejectLoopbackUrl() {
        assertThrows(WarningException.class, () -> guard.validateUrl("http://127.0.0.1:8080/sse"));
    }

    @Test
    void rejectPrivateNetwork() {
        assertThrows(WarningException.class, () -> guard.validateUrl("http://192.168.1.10/mcp"));
    }

    @Test
    void acceptPublicHttps() {
        assertDoesNotThrow(() -> guard.validateUrl("https://example.com/mcp"));
    }
}
