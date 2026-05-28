package io.github.genkidoudou.auth.oauth2.client;

import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.oauth2.Oauth2Properties;
import io.github.genkidoudou.web.system.oauthprovider.domain.SysOauthProvider;
import io.github.genkidoudou.web.system.oauthprovider.service.SysOauthProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录页可用的外部 IdP 列表（匿名）。
 */
@Tag(name = "登录-联邦IdP")
@RestController
@RequestMapping("/oauth/login")
@RequiredArgsConstructor
public class OAuth2LoginProvidersController {

    private final SysOauthProviderService providerService;
    private final Oauth2Properties oauth2Properties;

    @Operation(summary = "已启用的外部登录提供方")
    @GetMapping("/providers")
    public R<List<Map<String, String>>> providers() {
        if (!oauth2Properties.getClient().isEnabled()) {
            return R.ok(List.of());
        }
        List<Map<String, String>> list = new ArrayList<>();
        for (SysOauthProvider p : providerService.listEnabledForLogin()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("providerCode", p.getProviderCode());
            item.put("providerName", p.getProviderName());
            item.put("authorizePath", "/oauth2/client/authorize/" + p.getProviderCode());
            list.add(item);
        }
        return R.ok(list);
    }
}
