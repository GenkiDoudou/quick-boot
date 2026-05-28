package io.github.genkidoudou.web.system.oauthclient.service;

import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;

/**
 * OAuth 客户端「是否验签」字段约定（{@code sign_verify}：0 否 / 1 是）。
 */
public final class OauthClientSignVerifySupport {

    private OauthClientSignVerifySupport() {
    }

    /**
     * @param client 客户端
     * @return 为 {@code true} 时需执行 Client HMAC 与接口路径校验
     */
    public static boolean isSignVerifyEnabled(SysOauthClient client) {
        if (client == null) {
            return true;
        }
        String flag = client.getSignVerify();
        return flag == null || flag.isEmpty() || "1".equals(flag);
    }
}
