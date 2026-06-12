package io.github.genkidoudou.web.knowledge.mcp.support;

import io.github.genkidoudou.common.security.firewall.password.DefaultPasswordCodec;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.knowledge.constants.McpEnvValueType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link McpSecretSupport} 单元测试。
 */
class McpSecretSupportTest {

    private PasswordCodec codec;

    @BeforeEach
    void setUp() {
        codec = new DefaultPasswordCodec();
        Properties props = new Properties();
        props.setProperty("sm4.defaultKeyId", "default");
        props.setProperty("sm4.keys.default", "0123456789abcdef0123456789abcdef");
        codec.setProperties(props);
    }

    @Test
    void encodeAndResolveSecret() {
        String encoded = McpSecretSupport.encodeForStorage(codec, "secret-value");
        assertTrue(encoded.startsWith("{sm4"));
        assertEquals("secret-value", McpSecretSupport.resolvePlainSecret(codec, encoded));
    }

    @Test
    void maskSecretByDefault() {
        assertEquals(McpSecretSupport.MASK,
            McpSecretSupport.maskForDisplay(McpEnvValueType.SECRET, "cipher", false));
    }

    @Test
    void keepExistingSecretWhenBlank() {
        assertTrue(McpSecretSupport.isKeepExistingSecret(McpEnvValueType.SECRET, ""));
        assertFalse(McpSecretSupport.isKeepExistingSecret(McpEnvValueType.SECRET, "new"));
    }
}
