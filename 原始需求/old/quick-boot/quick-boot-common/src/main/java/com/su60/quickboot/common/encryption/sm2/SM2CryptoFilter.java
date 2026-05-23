//package com.su60.quickboot.common.encryption.sm2;
//
//import cn.hutool.core.io.IoUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.json.JSONUtil;
//import com.su60.quickboot.common.core.R;
//import com.su60.quickboot.common.security.config.SecurityProperties;
//import com.su60.quickboot.common.utils.ServletUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * SM2加密解密过滤器
// * <p>
// * 功能特性：
// * 1. 支持IP白名单，白名单内的IP不参与加密解密
// * 2. 对请求参数和Body进行解密
// * 3. 对响应结果进行加密
// * 4. 使用国密SM2算法
// * </p>
// *
// * @author luyanan
// * @since 2026/01/31
// */
//@Slf4j
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE + 5)
//@RequiredArgsConstructor
//@ConditionalOnProperty(prefix = "security.crypto", name = "enabled", havingValue = "true")
//public class SM2CryptoFilter extends OncePerRequestFilter {
//
//	private final SecurityProperties securityProperties;
//	private final AntPathMatcher pathMatcher = new AntPathMatcher();
//
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//			throws ServletException, IOException {
//
//		SecurityProperties.Crypto cryptoConfig = securityProperties.getCrypto();
//
//		// 1. 检查是否启用加密
//		if (!Boolean.TRUE.equals(cryptoConfig.getEnabled())) {
//			filterChain.doFilter(request, response);
//			return;
//		}
//
//		// 2. 检查是否在忽略URL列表中
//		if (shouldIgnoreUrl(request, cryptoConfig)) {
//			if (log.isDebugEnabled()) {
//				log.debug("[SM2加密] URL在忽略列表中，跳过加密: {}", request.getRequestURI());
//			}
//			filterChain.doFilter(request, response);
//			return;
//		}
//
//		// 3. 检查IP白名单
//		String clientIp = ServletUtil.getClientIP(request);
//		if (isIpInWhitelist(clientIp, cryptoConfig)) {
//			if (log.isDebugEnabled()) {
//				log.debug("[SM2加密] IP在白名单中，跳过加密: {}", clientIp);
//			}
//			filterChain.doFilter(request, response);
//			return;
//		}
//
//		try {
//			// 4. 包装请求，解密请求数据
//			SM2DecryptRequestWrapper decryptedRequest = new SM2DecryptRequestWrapper(request, cryptoConfig);
//
//			// 5. 包装响应，加密响应数据
//			SM2EncryptResponseWrapper encryptedResponse = new SM2EncryptResponseWrapper(response, cryptoConfig);
//
//			// 6. 继续处理请求
//			filterChain.doFilter(decryptedRequest, encryptedResponse);
//
//			// 7. 加密响应数据
//			encryptedResponse.encryptAndWrite();
//
//		} catch (SM2CryptoException e) {
//			// 加密解密失败
//			log.warn("[SM2加密] 处理失败: url={}, ip={}, error={}",
//				request.getRequestURI(), clientIp, e.getMessage());
//			handleCryptoError(response, e.getMessage());
//		} catch (Exception e) {
//			// 其他异常
//			log.error("[SM2加密] 处理异常: url={}, ip={}", request.getRequestURI(), clientIp, e);
//			handleCryptoError(response, "加密解密处理异常");
//		}
//	}
//
//	/**
//	 * 检查URL是否在忽略列表中
//	 */
//	private boolean shouldIgnoreUrl(HttpServletRequest request, SecurityProperties.Crypto cryptoConfig) {
//		String requestUri = request.getRequestURI();
//		return cryptoConfig.getIgnoreUrls().stream()
//				.anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
//	}
//
//	/**
//	 * 检查IP是否在白名单中
//	 */
//	private boolean isIpInWhitelist(String clientIp, SecurityProperties.Crypto cryptoConfig) {
//		if (cryptoConfig.getIpWhitelist() == null || cryptoConfig.getIpWhitelist().isEmpty()) {
//			return false;
//		}
//		return cryptoConfig.getIpWhitelist().contains(clientIp);
//	}
//
//	/**
//	 * 处理加密解密错误
//	 */
//	private void handleCryptoError(HttpServletResponse response, String message) throws IOException {
//		response.setStatus(HttpStatus.BAD_REQUEST.value());
//		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//
//		R<?> result = R.failed(HttpStatus.BAD_REQUEST.value(), message);
//		response.getWriter().write(JSONUtil.toJsonStr(result));
//	}
//}
//
