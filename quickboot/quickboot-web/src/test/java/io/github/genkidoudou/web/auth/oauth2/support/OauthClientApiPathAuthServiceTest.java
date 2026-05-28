package io.github.genkidoudou.web.auth.oauth2.support;

import io.github.genkidoudou.web.system.oauthclient.service.OauthClientApiPathAuthService;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link OauthClientApiPathAuthService} 单元测试。
 */
class OauthClientApiPathAuthServiceTest {

    private final OauthClientApiPathAuthService service = new OauthClientApiPathAuthService();

    @Test
    void assertPathAllowed_matchesAntPattern() {
        SysOauthClient client = new SysOauthClient();
        client.setApiPathPatterns("/open-api/v1/userinfo\n/system/**");

        assertThatCode(() -> service.assertPathAllowed(client, "/open-api/v1/userinfo"))
                .doesNotThrowAnyException();
        assertThatCode(() -> service.assertPathAllowed(client, "/system/oauthClient/list"))
                .doesNotThrowAnyException();
    }

    @Test
    void assertPathAllowed_rejectsUnmatched() {
        SysOauthClient client = new SysOauthClient();
        client.setApiPathPatterns("/open-api/**");

        assertThatThrownBy(() -> service.assertPathAllowed(client, "/system/user/list"))
                .isInstanceOf(WarningException.class)
                .hasMessageContaining("无权访问");
    }

    @Test
    void validatePathPatterns_rejectsMissingLeadingSlash() {
        assertThatThrownBy(() -> service.validatePathPatterns("system/**"))
                .isInstanceOf(WarningException.class)
                .hasMessageContaining("须以 / 开头");
    }
}
