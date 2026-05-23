package io.github.genkidoudou.web.auth.oauth2.server;

import cn.dev33.satoken.oauth2.processor.SaOAuth2ServerProcessor;
import io.github.genkidoudou.common.oauth2.Oauth2Properties;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth2 授权服务器路由入口（Sa-Token 标准 {@code /oauth2/*}）。
 */
@Hidden
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.oauth2.server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SaOAuth2ServerController {

    private final Oauth2Properties oauth2Properties;

    /**
     * 将 {@code /oauth2/*} 分发至 Sa-Token OAuth2 Server 处理器。
     */
    @RequestMapping("/oauth2/*")
    public Object oauth2Route() {
        if (!oauth2Properties.getServer().isEnabled()) {
            return "OAuth2 Server disabled";
        }
        return SaOAuth2ServerProcessor.instance.dister();
    }
}
