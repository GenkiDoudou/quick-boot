package io.github.genkidoudou.web.auth.oauth2;

import cn.dev33.satoken.oauth2.consts.GrantType;
import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import io.github.genkidoudou.common.oauth2.Oauth2Properties;
import io.github.genkidoudou.web.auth.oauth2.server.Oauth2ClientModelConverter;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Grant 全局开关与 client grant_types 合并逻辑。
 */
class Oauth2ClientModelConverterTest {

    @Test
    void prodDisablesPasswordGrant() {
        Oauth2Properties props = new Oauth2Properties();
        props.getServer().getGrant().setPasswordEnabled(false);
        SysOauthClient row = new SysOauthClient();
        row.setClientId("demo");
        row.setRedirectUris("http://localhost/cb");
        row.setGrantTypes("authorization_code,password,refresh_token");
        row.setScopes("openid");
        SaClientModel model = Oauth2ClientModelConverter.toSaModel(row, props, "secret");
        assertTrue(model.getAllowGrantTypes().contains(GrantType.authorization_code));
        assertTrue(!model.getAllowGrantTypes().contains(GrantType.password));
    }
}
