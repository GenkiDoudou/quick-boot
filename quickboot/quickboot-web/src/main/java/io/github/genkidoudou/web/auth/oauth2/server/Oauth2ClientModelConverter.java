package io.github.genkidoudou.web.auth.oauth2.server;

import cn.dev33.satoken.oauth2.consts.GrantType;
import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.oauth2.Oauth2Properties;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 将 {@link SysOauthClient} 转为 Sa-Token {@link SaClientModel}，并叠加全局 Grant 开关。
 */
public final class Oauth2ClientModelConverter {

    private Oauth2ClientModelConverter() {
    }

    /**
     * @param row    库表记录
     * @param props  全局 OAuth2 配置
     * @param plainSecret 已解密的 client_secret（仅内存使用）
     */
    public static SaClientModel toSaModel(SysOauthClient row, Oauth2Properties props, String plainSecret) {
        SaClientModel model = new SaClientModel();
        model.setClientId(row.getClientId());
        model.setClientSecret(plainSecret);
        if (StrUtil.isNotBlank(row.getScopes())) {
            for (String scope : row.getScopes().split("[,\\s]+")) {
                if (StrUtil.isNotBlank(scope)) {
                    model.addContractScopes(scope.trim());
                }
            }
        }
        for (String uri : row.getRedirectUris().split("[,\\s]+")) {
            if (StrUtil.isNotBlank(uri)) {
                model.addAllowRedirectUris(uri.trim());
            }
        }
        Set<String> allowed = parseGrantTypes(row.getGrantTypes());
        if (allowed.contains("authorization_code")) {
            model.addAllowGrantTypes(GrantType.authorization_code);
        }
        if (allowed.contains("refresh_token")) {
            model.addAllowGrantTypes(GrantType.refresh_token);
        }
        if (allowed.contains("client_credentials")) {
            model.addAllowGrantTypes(GrantType.client_credentials);
        }
        if (allowed.contains("password") && props.getServer().getGrant().isPasswordEnabled()) {
            model.addAllowGrantTypes(GrantType.password);
        }
        if (allowed.contains("implicit") && props.getServer().getGrant().isImplicitEnabled()) {
            model.addAllowGrantTypes(GrantType.implicit);
        }
        return model;
    }

    private static Set<String> parseGrantTypes(String grantTypes) {
        if (StrUtil.isBlank(grantTypes)) {
            return Set.of("authorization_code", "refresh_token");
        }
        return Arrays.stream(grantTypes.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
    }
}
