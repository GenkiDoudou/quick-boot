package io.github.genkidoudou.web.system.oauthclient.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;

/**
 * OAuth2 client_secret / IdP secret 明文解析（SM4 可逆；否则视为明文开发存根）。
 */
public final class Oauth2SecretSupport {

    private Oauth2SecretSupport() {
    }

    /**
     * @param codec  项目密码编解码器
     * @param stored 库中密文或明文
     * @return OAuth2 校验用明文 secret
     */
    public static String resolvePlainSecret(PasswordCodec codec, String stored) {
        if (StrUtil.isBlank(stored)) {
            return "";
        }
        String s = stored.trim();
        if (s.startsWith("{sm4")) {
            return codec.decrypt(s);
        }
        return s;
    }

    /**
     * 入库加密：优先 SM4，调用方传入 keyId 由全局配置决定时使用默认 sm4。
     */
    public static String encodeForStorage(PasswordCodec codec, String plain) {
        if (StrUtil.isBlank(plain)) {
            return "";
        }
        return codec.encrypt(plain, "sm4");
    }
}
