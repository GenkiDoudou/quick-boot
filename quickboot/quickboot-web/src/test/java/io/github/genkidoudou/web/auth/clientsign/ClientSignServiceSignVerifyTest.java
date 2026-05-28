package io.github.genkidoudou.web.auth.clientsign;

import io.github.genkidoudou.web.system.oauthclient.clientsign.CachedBodyHttpServletRequest;
import io.github.genkidoudou.web.system.oauthclient.clientsign.ClientSignProperties;
import io.github.genkidoudou.web.system.oauthclient.clientsign.ClientSignService;
import io.github.genkidoudou.web.system.oauthclient.service.OauthClientApiPathAuthService;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import io.github.genkidoudou.web.system.oauthclient.mapper.SysOauthClientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

/**
 * {@link ClientSignService} 在 {@code sign_verify=0} 时跳过验签。
 */
@ExtendWith(MockitoExtension.class)
class ClientSignServiceSignVerifyTest {

    @Mock
    private SysOauthClientMapper oauthClientMapper;

    private ClientSignService clientSignService;

    @BeforeEach
    void setUp() {
        ClientSignProperties properties = new ClientSignProperties();
        properties.setEnabled(true);
        properties.setNonceCacheName("clientSignNonceTest2");
        clientSignService = new ClientSignService(
                properties,
                oauthClientMapper,
                null,
                new ConcurrentMapCacheManager(properties.getNonceCacheName()),
                new OauthClientApiPathAuthService());
    }

    @Test
    void verify_skipsWhenSignVerifyDisabled() throws Exception {
        SysOauthClient client = new SysOauthClient();
        client.setClientId("no-sign");
        client.setSignVerify("0");
        client.setStatus("0");
        client.setDelFlag("0");
        when(oauthClientMapper.selectById("no-sign")).thenReturn(client);

        MockHttpServletRequest raw = new MockHttpServletRequest("GET", "/system/user/list");
        raw.addHeader("X-Client-Id", "no-sign");
        CachedBodyHttpServletRequest request = new CachedBodyHttpServletRequest(raw);

        assertThatCode(() -> clientSignService.verify(request)).doesNotThrowAnyException();
    }
}
