package io.github.genkidoudou.web.auth.clientsign;

import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.auth.oauth2.support.OauthClientApiPathAuthService;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import io.github.genkidoudou.web.system.oauthclient.mapper.SysOauthClientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@link ClientSignService} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ClientSignServiceTest {

    private static final String CLIENT_ID = "quick-ui";
    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Mock
    private SysOauthClientMapper oauthClientMapper;

    @Mock
    private PasswordCodec passwordCodec;

    private ClientSignService clientSignService;

    @BeforeEach
    void setUp() {
        ClientSignProperties properties = new ClientSignProperties();
        properties.setEnabled(true);
        properties.setNonceCacheName("clientSignNonceTest");
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(properties.getNonceCacheName());
        OauthClientApiPathAuthService apiPathAuthService = new OauthClientApiPathAuthService();
        clientSignService = new ClientSignService(properties, oauthClientMapper, passwordCodec, cacheManager,
                apiPathAuthService);

        SysOauthClient client = new SysOauthClient();
        client.setClientId(CLIENT_ID);
        client.setClientSecret(SECRET);
        client.setStatus("0");
        client.setDelFlag("0");
        client.setSignVerify("1");
        client.setApiPathPatterns("/login\n/getInfo");
        when(oauthClientMapper.selectById(CLIENT_ID)).thenReturn(client);
    }

    @Test
    void verify_acceptsValidSignature() throws Exception {
        byte[] body = new byte[0];
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = "abc123nonce0000001";
        String canonical = ClientSignService.buildCanonical("POST", "/login", body, timestamp, nonce, CLIENT_ID);
        String signature = hmacSha256Base64(SECRET, canonical);

        CachedBodyHttpServletRequest request = buildRequest("POST", "/login", body,
                CLIENT_ID, timestamp, nonce, signature);

        assertThatCode(() -> clientSignService.verify(request)).doesNotThrowAnyException();
    }

    @Test
    void verify_rejectsReplayNonce() throws Exception {
        byte[] body = new byte[0];
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = "replay-nonce-00000002";
        String canonical = ClientSignService.buildCanonical("GET", "/getInfo", body, timestamp, nonce, CLIENT_ID);
        String signature = hmacSha256Base64(SECRET, canonical);

        CachedBodyHttpServletRequest first = buildRequest("GET", "/getInfo", body,
                CLIENT_ID, timestamp, nonce, signature);
        clientSignService.verify(first);

        CachedBodyHttpServletRequest second = buildRequest("GET", "/getInfo", body,
                CLIENT_ID, timestamp, nonce, signature);
        assertThatThrownBy(() -> clientSignService.verify(second))
                .isInstanceOf(WarningException.class)
                .hasMessageContaining("nonce");
    }

    private static CachedBodyHttpServletRequest buildRequest(String method, String path, byte[] body,
                                                             String clientId, String timestamp, String nonce,
                                                             String signature) throws Exception {
        MockHttpServletRequest raw = new MockHttpServletRequest(method, path);
        raw.setContent(body);
        raw.addHeader("X-Client-Id", clientId);
        raw.addHeader("X-Client-Timestamp", timestamp);
        raw.addHeader("X-Client-Nonce", nonce);
        raw.addHeader("X-Client-Signature", signature);
        return new CachedBodyHttpServletRequest(raw);
    }

    private static String hmacSha256Base64(String secret, String canonical) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return java.util.Base64.getEncoder().encodeToString(raw);
    }
}
