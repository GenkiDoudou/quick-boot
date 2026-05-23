package io.github.genkidoudou.web.auth.oauth2.client;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.oauth2.Oauth2Properties;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.auth.oauth2.support.Oauth2SecretSupport;
import io.github.genkidoudou.web.system.oauthprovider.domain.SysOauthProvider;
import io.github.genkidoudou.web.system.oauthprovider.domain.SysOauthUserBind;
import io.github.genkidoudou.web.system.oauthprovider.mapper.SysOauthUserBindMapper;
import io.github.genkidoudou.web.system.oauthprovider.service.SysOauthProviderService;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 Client 角色：外部 IdP 授权码换本地会话。
 */
@Service
@RequiredArgsConstructor
public class OAuth2ClientService {

    private static final TimedCache<String, String> STATE_CACHE = CacheUtil.newTimedCache(10 * 60 * 1000L);

    static {
        STATE_CACHE.schedulePrune(60_000L);
    }

    private final SysOauthProviderService providerService;
    private final SysOauthUserBindMapper bindMapper;
    private final PasswordCodec passwordCodec;
    private final Oauth2Properties oauth2Properties;

    /**
     * 构造跳转外部 IdP 的 URL。
     */
    public String buildAuthorizeUrl(String providerCode) {
        SysOauthProvider provider = requireEnabled(providerCode);
        String state = IdUtil.fastSimpleUUID();
        STATE_CACHE.put(state, providerCode);
        String redirect = urlEncode(provider.getRedirectUri());
        return provider.getAuthorizeUrl()
                + (provider.getAuthorizeUrl().contains("?") ? "&" : "?")
                + "response_type=code&client_id=" + urlEncode(provider.getClientId())
                + "&redirect_uri=" + redirect
                + "&scope=openid%20profile&state=" + state;
    }

    /**
     * 处理回调并返回带 token 的前端地址。
     */
    public String handleCallback(String providerCode, String code, String state) {
        if (StrUtil.isBlank(code) || StrUtil.isBlank(state)) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "授权回调参数无效");
        }
        String cached = STATE_CACHE.get(state, false);
        if (!providerCode.equals(cached)) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "state 校验失败");
        }
        STATE_CACHE.remove(state);

        SysOauthProvider provider = requireEnabled(providerCode);
        String secret = Oauth2SecretSupport.resolvePlainSecret(passwordCodec, provider.getClientSecret());
        JSONObject tokenJson = exchangeCode(provider, code, secret);
        String accessToken = tokenJson.getStr("access_token");
        if (StrUtil.isBlank(accessToken)) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "换取 access_token 失败");
        }
        String subject = fetchSubject(provider, accessToken);

        SysOauthUserBind bind = bindMapper.selectOne(Wrappers.<SysOauthUserBind>lambdaQuery()
                .eq(SysOauthUserBind::getProviderCode, providerCode)
                .eq(SysOauthUserBind::getExternalSubject, subject)
                .last("LIMIT 1"));
        if (bind == null) {
            if (!"1".equals(provider.getAutoRegister())) {
                throw new WarningException(ErrorCodes.Security.FORBIDDEN, "未绑定本地账号，请联系管理员");
            }
            throw new WarningException(ErrorCodes.Security.FORBIDDEN, "未开启自动注册，请先绑定账号");
        }

        StpUtil.login(bind.getUserId());
        String token = StpUtil.getTokenValue();
        String front = oauth2Properties.getClient().getDefaultRedirectAfterLogin();
        return front + (front.contains("?") ? "&" : "?") + "access_token=" + urlEncode(token);
    }

    private SysOauthProvider requireEnabled(String providerCode) {
        SysOauthProvider provider = providerService.getByCode(providerCode);
        if (provider == null || !"1".equals(provider.getEnabled())) {
            throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "IdP 未配置或未启用");
        }
        return provider;
    }

    private JSONObject exchangeCode(SysOauthProvider provider, String code, String secret) {
        HttpResponse resp = HttpRequest.post(provider.getTokenUrl())
                .form("grant_type", "authorization_code")
                .form("code", code)
                .form("redirect_uri", provider.getRedirectUri())
                .form("client_id", provider.getClientId())
                .form("client_secret", secret)
                .timeout(15000)
                .execute();
        if (!resp.isOk()) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "IdP token 接口失败");
        }
        return JSONUtil.parseObj(resp.body());
    }

    private String fetchSubject(SysOauthProvider provider, String accessToken) {
        if (StrUtil.isBlank(provider.getUserinfoUrl())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "未配置 userinfo_url");
        }
        HttpResponse resp = HttpRequest.get(provider.getUserinfoUrl())
                .header("Authorization", "Bearer " + accessToken)
                .timeout(15000)
                .execute();
        if (!resp.isOk()) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "拉取用户信息失败");
        }
        JSONObject json = JSONUtil.parseObj(resp.body());
        String sub = json.getStr("sub");
        if (StrUtil.isBlank(sub)) {
            sub = json.getStr("id");
        }
        if (StrUtil.isBlank(sub)) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "外部用户标识为空");
        }
        return sub;
    }

    private static String urlEncode(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}
