package com.su60.quickboot.web.config;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.core.R;
import com.su60.quickboot.common.security.OauthClientVo;
import com.su60.quickboot.system.dos.SysOauthClientDo;
import com.su60.quickboot.system.service.ISysOauthClientService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 客户端配置
 *
 * @author luyanan
 * @since 2026/2/9
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE )
@RequiredArgsConstructor
@Configuration
public class OauthClientFilter extends OncePerRequestFilter {

	private final ISysOauthClientService sysOauthClientService;


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String clientId = request.getHeader("client_id");
		if (StrUtil.isBlank(clientId)) {
			R.failed(response, 401, "请设置client_id");
		} else {
			SysOauthClientDo enableByClientId = sysOauthClientService.getEnableByClientId(clientId);
			if (enableByClientId == null) {
				R.failed(response, 401, "请设置正确的client_id");
			} else {

				OauthClientVo oauthClientVo = BeanUtil.copyProperties(enableByClientId, OauthClientVo.class);
				request.setAttribute("oauthClient", oauthClientVo);
				filterChain.doFilter(request, response);
			}
		}
	}
}
