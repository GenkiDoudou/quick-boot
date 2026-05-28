package io.github.genkidoudou.web.auth.clientsign;

import io.github.genkidoudou.web.system.oauthclient.clientsign.ClientRequestPathSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientRequestPathSupportTest {

    @Test
    void stripGatewayPrefix_removesProdApi() {
        assertThat(ClientRequestPathSupport.stripGatewayPrefix("/prod-api/login")).isEqualTo("/login");
        assertThat(ClientRequestPathSupport.stripGatewayPrefix("/dev-api/getInfo")).isEqualTo("/getInfo");
        assertThat(ClientRequestPathSupport.stripGatewayPrefix("/login")).isEqualTo("/login");
    }
}
