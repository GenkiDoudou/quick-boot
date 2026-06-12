package io.github.genkidoudou.web.ai.support;

import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.ai.constants.AiApiKeyType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AiSecretSupport} 单元测试。
 */
class AiSecretSupportTest {

    @Test
    void maskSecretWhenNotReveal() {
        String masked = AiSecretSupport.maskForDisplay(AiApiKeyType.SECRET, "{sm4:abc}", false);
        assertEquals(AiSecretSupport.MASK, masked);
    }

    @Test
    void keepExistingSecretWhenBlankSubmitted() {
        assertTrue(AiSecretSupport.isKeepExistingSecret(AiApiKeyType.SECRET, ""));
        assertTrue(AiSecretSupport.isKeepExistingSecret(AiApiKeyType.SECRET, null));
    }

    @Test
    void resolvePlainSecretFromSm4() {
        PasswordCodec codec = Mockito.mock(PasswordCodec.class);
        Mockito.when(codec.decrypt("{sm4:x}")).thenReturn("plain-key");
        assertEquals("plain-key", AiSecretSupport.resolvePlainSecret(codec, "{sm4:x}"));
    }
}
