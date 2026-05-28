package io.github.genkidoudou.auth.oauth2.config;

import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.dev33.satoken.oauth2.strategy.SaOAuth2Strategy;
import io.github.genkidoudou.common.oauth2.Oauth2Properties;
import io.github.genkidoudou.auth.oauth2.server.OAuth2LoginBridgeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 OAuth2 DataLoader、Scope 分级与登录桥接策略。
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.oauth2.server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Oauth2ServerConfiguration {

    private final OAuth2LoginBridgeService loginBridge;
    private final Oauth2Properties oauth2Properties;

    @PostConstruct
    public void configureOAuth2Server() {
        SaOAuth2ServerConfig cfg = SaOAuth2Manager.getServerConfig();
        cfg.setHigherScope("openid");
        cfg.setLowerScope("profile");
        cfg.setEnablePassword(oauth2Properties.getServer().getGrant().isPasswordEnabled());
        cfg.setEnableImplicit(oauth2Properties.getServer().getGrant().isImplicitEnabled());

        SaOAuth2Strategy.instance.doLoginHandle = (name, pwd) -> {
            String captchaId = cn.dev33.satoken.context.SaHolder.getRequest().getParam("captchaId");
            return loginBridge.doOAuth2Login(name, pwd, captchaId);
        };
    }
}
