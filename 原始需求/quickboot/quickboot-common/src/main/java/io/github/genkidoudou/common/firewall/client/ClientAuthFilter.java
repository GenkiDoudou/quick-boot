package io.github.genkidoudou.common.firewall.client;

import io.github.genkidoudou.common.firewall.client.exception.ClientAuthException;
import io.github.genkidoudou.common.core.R;
import io.github.genkidoudou.common.exception.ErrorCode;
import io.github.genkidoudou.common.i18n.I18nUtil;
import io.github.genkidoudou.common.utils.ServletUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * 客户端认证过滤器
 *
 * @author luyanan
 * @since 2026-03-04
 */
@Slf4j
public class ClientAuthFilter implements Filter {

    private final ClientService clientService;
    private final ClientProperties clientProperties;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public ClientAuthFilter(ClientService clientService,
                            ClientProperties clientProperties) {
        this.clientService = clientService;
        this.clientProperties = clientProperties;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("客户端认证过滤器已初始化");
        log.info("数据源类型: {}", clientProperties.getSource());
        log.info("排除的URL: {}", clientProperties.getExcludeUrls());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        // 类型转换
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 检查是否启用
        if (!Boolean.TRUE.equals(clientProperties.getEnabled())) {
            chain.doFilter(request, response);
            return;
        }

        // 检查是否在排除列表中
        String requestUri = req.getRequestURI();
        if (isExcluded(requestUri)) {
            log.debug("请求 {} 在排除列表中，跳过客户端认证", requestUri);
            chain.doFilter(request, response);
            return;
        }

        // 获取客户端信息
        String clientIdStr = req.getHeader(clientProperties.getClientIdHeader());


        if (!StringUtils.hasText(clientIdStr)) {
            log.warn("缺少客户端认证信息, URI: {}", requestUri);
            ServletUtils.writeResponse(resp, ErrorCode.CLIENT_NOT_FOUND);
            return;
        }
        try {
            OauthClient oauthClient = clientService.parserClientId(clientIdStr);
            log.debug("客户端认证成功, clientId: {}", oauthClient);
            req.setAttribute("oauth_client", oauthClient);
        } catch (ClientAuthException e) {
            e.printStackTrace();
            ServletUtils.writeResponse(resp, e.getCode());
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        log.info("客户端认证过滤器已销毁");
    }

    /**
     * 检查URL是否在排除列表中
     */
    private boolean isExcluded(String requestUri) {
        List<String> excludeUrls = clientProperties.getExcludeUrls();
        if (excludeUrls == null || excludeUrls.isEmpty()) {
            return false;
        }

        for (String pattern : excludeUrls) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }

        return false;
    }


}
