package io.github.genkidoudou.web.auth.oauth2.client;

import io.github.genkidoudou.common.oauth2.Oauth2Properties;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 外部 IdP 联邦登录：authorize 重定向与 callback 处理。
 */
@Hidden
@RestController
@RequestMapping("/oauth2/client")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.oauth2.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OAuth2ClientController {

    private final OAuth2ClientService clientService;
    private final Oauth2Properties oauth2Properties;

    @GetMapping("/authorize/{provider}")
    public void authorize(@PathVariable String provider, HttpServletResponse response) throws IOException {
        if (!oauth2Properties.getClient().isEnabled()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.sendRedirect(clientService.buildAuthorizeUrl(provider));
    }

    @GetMapping("/callback/{provider}")
    public void callback(@PathVariable String provider,
                         @RequestParam(required = false) String code,
                         @RequestParam(required = false) String state,
                         HttpServletResponse response) throws IOException {
        String redirect = clientService.handleCallback(provider, code, state);
        response.sendRedirect(redirect);
    }
}
