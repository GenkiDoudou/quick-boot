package io.github.genkidoudou.web.system.oauthclient.clientsign;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.servlet.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 全局 Client HMAC 签名校验过滤器（优先于登录拦截）。
 * <p>
 * Filter 内异常不会进入 {@code @RestControllerAdvice}，须在本类写出与 {@link io.github.genkidoudou.common.api.R} 一致的 JSON。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class ClientSignFilter extends OncePerRequestFilter {

    private final ClientSignProperties properties;
    private final ClientSignService clientSignService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request);
            clientSignService.verify(wrapped);
            filterChain.doFilter(wrapped, response);
        } catch (WarningException ex) {
            ServletUtils.writeResponse(response, ex.getCode(), ex.getMsg());
        } catch (Exception ex) {
            log.error("Client 签名校验过滤器异常, uri={}", request.getRequestURI(), ex);
            ServletUtils.writeResponse(response, ErrorCodes.System.DEPENDENCY_UNAVAILABLE, "服务暂不可用，请稍后重试");
        }
    }
}
