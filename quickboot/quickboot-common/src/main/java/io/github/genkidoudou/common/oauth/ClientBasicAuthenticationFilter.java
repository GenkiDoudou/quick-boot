package io.github.genkidoudou.common.oauth;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.crypto.PasswordCodec;
import io.github.genkidoudou.common.crypto.PasswordCodecFactories;
import io.github.genkidoudou.common.oauth.config.OauthClientProperties;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import io.github.genkidoudou.common.utils.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 客户端校验(在未登录的状态下校验)
 *
 * @author luyanan
 * @since 2026/7/27
 */
@RequiredArgsConstructor
@Slf4j
public class ClientBasicAuthenticationFilter extends OncePerRequestFilter {


  private final OauthServiceSupport oauthServiceSupport;

  private final OauthClientProperties oauthClientProperties;


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    String method = request.getMethod();
    if (method.equals(HttpMethod.OPTIONS.name())) {
      filterChain.doFilter(request, response);
      return;
    }
    if (ServletUtils.matchesAny(request, oauthClientProperties.getIgnoreUrl())) {
      filterChain.doFilter(request, response);
      return;
    }
    PasswordCodec passwordCodec = PasswordCodecFactories.get("clientBasic");

    String header = request.getHeader(LoginUserUtils.TOKEN_HEADER);
    // 用户 Bearer：不做 Client Basic（须先于 getLoginUser，避免误解密触发 500）
    if (StrUtil.startWithIgnoreCase(header, "Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    LoginUser loginUser = LoginUserUtils.getLoginUser();
    if (null == loginUser) {
      if (StrUtil.isBlank(header)) {
        ServletUtils.writeResponse(response, 600);
        return;
      }
      if (!StrUtil.startWithIgnoreCase(header, LoginUserUtils.BASIC)) {
        ServletUtils.writeResponse(response, 600);
        return;
      }
      try {
        String obfuscated = header.substring(LoginUserUtils.BASIC.length()).trim();
        String decrypt = passwordCodec.decrypt(obfuscated);
        String[] parts = decrypt.split(":", 2);
        if (parts.length < 2 || StrUtil.hasBlank(parts[0], parts[1])) {
          ServletUtils.writeResponse(response, 600);
          return;
        }
        String clientId = parts[0];
        String clientSecret = parts[1];

        OauthClientVo oauthClientVo = oauthServiceSupport.findByClientId(clientId);
        if (null == oauthClientVo) {
          ServletUtils.writeResponse(response, 600);
          return;
        }
        if (!clientSecret.equals(oauthClientVo.getClientSecret())) {
          ServletUtils.writeResponse(response, 601);
          return;
        }

        if (!ServletUtils.matchesAny(request, oauthClientVo.getApiPathPatterns())) {
          ServletUtils.writeResponse(response, 602);
          return;
        }
        request.setAttribute(OauthClientVo.ATTR_KEY, oauthClientVo);
      } catch (Exception ex) {
        ex.printStackTrace();
        log.warn("Client Basic auth failed: {}", ex.getMessage());
        ServletUtils.writeResponse(response, 600);
        return;
      }
    }
    filterChain.doFilter(request, response);

  }
}
