package io.github.genkidoudou.auth.filter;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.auth.utils.LoginUserUtils;
import io.github.genkidoudou.auth.vo.LoginUser;
import io.github.genkidoudou.common.crypto.PasswordCodec;
import io.github.genkidoudou.common.crypto.PasswordCodecFactories;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
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

  private final PasswordEncoderFactories passwordEncoderFactories;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    String method = request.getMethod();
    if (method.equals(HttpMethod.OPTIONS.name())) {
      return;
    }

    PasswordCodec passwordCodec = PasswordCodecFactories.get("clientBasic");

    LoginUser loginUser = LoginUserUtils.getLoginUser();
    if (null == loginUser) {
      // 当未登录的时候 从header 中获取
      String header = request.getHeader(LoginUserUtils.TOKEN_HEADER);
      if (StrUtil.isBlank(header)) {

        

      }
    }

  }
}
