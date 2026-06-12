package io.github.genkidoudou.auth.oauth2.open;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.template.SaOAuth2Util;
import io.github.genkidoudou.web.system.oauthclient.service.OauthClientApiPathAuthService;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import io.github.genkidoudou.web.system.oauthclient.mapper.SysOauthClientMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * {@code /open-api/**} 开放 API 拦截器：校验 OAuth2 access_token 并检查客户端 Ant 路径授权。
 * <p>
 * 与 {@link io.github.genkidoudou.exception.GlobalExceptionHandler} 不同：
 * 本拦截器直接写 HTTP 状态码（401/403），<b>不返回</b>统一 {@code R} JSON 体，
 * 便于 machine-to-machine 调用方按状态码处理。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class OpenApiOAuth2Interceptor implements HandlerInterceptor {

    private final SysOauthClientMapper oauthClientMapper;
    private final OauthClientApiPathAuthService apiPathAuthService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            AccessTokenModel token = SaOAuth2Util.currentAccessToken();
            String clientId = token.clientId;
            SysOauthClient client = oauthClientMapper.selectById(clientId);
            if (client == null || !"0".equals(client.getDelFlag()) || !"0".equals(client.getStatus())) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return false;
            }
            String path = apiPathAuthService.resolveServletPath(request);
            apiPathAuthService.assertPathAllowed(client, path);
            return true;
        } catch (NotLoginException ex) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        } catch (WarningException ex) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        } catch (Exception ex) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }
}
