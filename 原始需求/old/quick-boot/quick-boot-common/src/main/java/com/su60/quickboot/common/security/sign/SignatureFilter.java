//package com.su60.quickboot.common.security.sign;
//
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
// * 接口签名验证过滤器（基于国密SM3算法）
// * <p>
// * 功能特性：
// * 1. 支持IP白名单，白名单内的IP不参与验签
// * 2. 从参数或Header中接收：timestamp（时间戳）、sign（签名）、nonce（随机字符串）
// * 3. GET请求和POST表单请求：直接使用参数参与验签
// * 4. POST JSON请求：将JSON字符串作为json参数参与验签（json=xxx）
// * 5. 使用国密SM3算法生成签名
// * </p>
// *
// * @author luyanan
// * @since 2026/01/31
// */
//@Slf4j
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE + 10)
//@RequiredArgsConstructor
//@ConditionalOnProperty(prefix = "security.sign", name = "enabled", havingValue = "true")
//public class SignatureFilter extends OncePerRequestFilter {
//
//	private final SecurityProperties securityProperties;
//	private final AntPathMatcher pathMatcher = new AntPathMatcher();
//
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//			throws ServletException, IOException {
//
//		SecurityProperties.Sign signConfig = securityProperties.getSign();
//
//		// 1. 检查是否启用签名验证
//		if (!Boolean.TRUE.equals(signConfig.getEnabled())) {
//			filterChain.doFilter(request, response);
//			return;
//		}
//
//		// 2. 检查是否在忽略URL列表中
//		if (shouldIgnoreUrl(request, signConfig)) {
//			if (log.isDebugEnabled()) {
//				log.debug("[签名验证] URL在忽略列表中，跳过验签: {}", request.getRequestURI());
//			}
//			filterChain.doFilter(request, response);
//			return;
//		}
//
//		// 3. 检查IP白名单
//		String clientIp = ServletUtil.getClientIP(request);
//		if (isIpInWhitelist(clientIp, signConfig)) {
//			if (log.isDebugEnabled()) {
//				log.debug("[签名验证] IP在白名单中，跳过验签: {}", clientIp);
//			}
//			filterChain.doFilter(request, response);
//			return;
//		}
//
//		try {
//			// 4. 包装请求以支持多次读取Body
//			CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
//
//			// 5. 收集签名参数
//			Map<String, String> signParams = collectSignParams(wrappedRequest);
//
//			// 6. 验证必要参数
//			validateRequiredParams(signParams);
//
//			// 7. 验证时间戳
//			validateTimestamp(signParams, signConfig);
//
//			// 8. 验证签名
//			validateSignature(signParams, signConfig);
//
//			// 9. 验证通过，继续处理请求
//			if (log.isDebugEnabled()) {
//				log.debug("[签名验证] 验证通过: url={}, ip={}", request.getRequestURI(), clientIp);
//			}
//			filterChain.doFilter(wrappedRequest, response);
//
//		} catch (SignatureException e) {
//			// 签名验证失败
//			log.warn("[签名验证] 验证失败: url={}, ip={}, error={}",
//				request.getRequestURI(), clientIp, e.getMessage());
//			handleSignatureError(response, e.getMessage());
//		} catch (Exception e) {
//			// 其他异常
//			log.error("[签名验证] 处理异常: url={}, ip={}", request.getRequestURI(), clientIp, e);
//			handleSignatureError(response, "签名验证处理异常");
//		}
//	}
//
//	/**
//	 * 检查URL是否在忽略列表中
//	 */
//	private boolean shouldIgnoreUrl(HttpServletRequest request, SecurityProperties.Sign signConfig) {
//		String requestUri = request.getRequestURI();
//		return signConfig.getIgnoreUrls().stream()
//				.anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
//	}
//
//	/**
//	 * 检查IP是否在白名单中
//	 */
//	private boolean isIpInWhitelist(String clientIp, SecurityProperties.Sign signConfig) {
//		if (signConfig.getIpWhitelist() == null || signConfig.getIpWhitelist().isEmpty()) {
//			return false;
//		}
//		return signConfig.getIpWhitelist().contains(clientIp);
//	}
//
//	/**
//	 * 收集签名参数
//	 */
//	private Map<String, String> collectSignParams(CachedBodyHttpServletRequest request) throws IOException {
//		Map<String, String> params = new HashMap<>();
//
//		// 1. 从Header中获取签名相关参数（优先从Header获取）
//		String timestamp = request.getHeader("timestamp");
//		String sign = request.getHeader("sign");
//		String nonce = request.getHeader("nonce");
//
//		// 如果Header中没有，再从参数中获取
//		if (StrUtil.isBlank(timestamp)) {
//			timestamp = request.getParameter("timestamp");
//		}
//		if (StrUtil.isBlank(sign)) {
//			sign = request.getParameter("sign");
//		}
//		if (StrUtil.isBlank(nonce)) {
//			nonce = request.getParameter("nonce");
//		}
//
//		if (StrUtil.isNotBlank(timestamp)) {
//			params.put("timestamp", timestamp);
//		}
//		if (StrUtil.isNotBlank(sign)) {
//			params.put("sign", sign);
//		}
//		if (StrUtil.isNotBlank(nonce)) {
//			params.put("nonce", nonce);
//		}
//
//		// 2. 根据请求类型收集业务参数
//		String method = request.getMethod();
//		String contentType = request.getContentType();
//
//		if ("GET".equalsIgnoreCase(method)) {
//			// GET请求：直接获取所有参数（不包括签名字段）
//			collectGetParams(request, params);
//		} else if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
//			if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
//				// POST JSON请求：将JSON字符串作为json参数
//				collectJsonParams(request, params);
//			} else {
//				// POST 表单请求：直接获取所有参数（不包括签名字段）
//				collectFormParams(request, params);
//			}
//		}
//
//		if (log.isDebugEnabled()) {
//			log.debug("[签名验证] 收集到的参数: {}", params);
//		}
//
//		return params;
//	}
//
//	/**
//	 * 从Header或参数中获取值（优先Header）
//	 */
//	private String getParamFromHeaderOrParam(HttpServletRequest request, String name) {
//		// 优先从Header获取
//		String value = request.getHeader(name);
//		if (StrUtil.isBlank(value)) {
//			// 从参数获取
//			value = request.getParameter(name);
//		}
//		return value;
//	}
//
//	/**
//	 * 收集GET请求参数（不包括签名字段）
//	 */
//	private void collectGetParams(HttpServletRequest request, Map<String, String> params) {
//		Map<String, String[]> parameterMap = request.getParameterMap();
//		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
//			String key = entry.getKey();
//			String[] values = entry.getValue();
//
//			// 跳过签名字段（这些字段已经从Header中获取）
//			if ("timestamp".equals(key) || "sign".equals(key) || "nonce".equals(key)) {
//				continue;
//			}
//
//			if (values != null && values.length > 0) {
//				// 只取第一个值
//				params.put(key, values[0]);
//			}
//		}
//	}
//
//	/**
//	 * 收集POST表单参数（不包括签名字段）
//	 */
//	private void collectFormParams(HttpServletRequest request, Map<String, String> params) {
//		Map<String, String[]> parameterMap = request.getParameterMap();
//		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
//			String key = entry.getKey();
//			String[] values = entry.getValue();
//
//			// 跳过签名字段（这些字段已经从Header中获取）
//			if ("timestamp".equals(key) || "sign".equals(key) || "nonce".equals(key)) {
//				continue;
//			}
//
//			if (values != null && values.length > 0) {
//				params.put(key, values[0]);
//			}
//		}
//	}
//
//	/**
//	 * 收集POST JSON参数
//	 */
//	private void collectJsonParams(CachedBodyHttpServletRequest request, Map<String, String> params) throws IOException {
//		String body = request.getBody();
//		if (StrUtil.isNotBlank(body)) {
//			// 将JSON字符串作为json参数
//			params.put("json", body);
//
//			if (log.isDebugEnabled()) {
//				log.debug("[签名验证] JSON请求体: {}", body);
//			}
//		}
//	}
//
//	/**
//	 * 验证必要参数
//	 */
//	private void validateRequiredParams(Map<String, String> params) {
//		if (!params.containsKey("timestamp") || StrUtil.isBlank(params.get("timestamp"))) {
//			throw new SignatureException("缺少必要参数: timestamp");
//		}
//		if (!params.containsKey("sign") || StrUtil.isBlank(params.get("sign"))) {
//			throw new SignatureException("缺少必要参数: sign");
//		}
//		if (!params.containsKey("nonce") || StrUtil.isBlank(params.get("nonce"))) {
//			throw new SignatureException("缺少必要参数: nonce");
//		}
//	}
//
//	/**
//	 * 验证时间戳
//	 */
//	private void validateTimestamp(Map<String, String> params, SecurityProperties.Sign signConfig) {
//		String timestampStr = params.get("timestamp");
//		try {
//			Long timestamp = Long.parseLong(timestampStr);
//			Integer expireTime = signConfig.getExpireTime();
//
//			if (!SM3SignatureUtils.isTimestampValid(timestamp, expireTime)) {
//				throw new SignatureException("时间戳已过期或无效");
//			}
//		} catch (NumberFormatException e) {
//			throw new SignatureException("时间戳格式错误");
//		}
//	}
//
//	/**
//	 * 验证签名
//	 */
//	private void validateSignature(Map<String, String> params, SecurityProperties.Sign signConfig) {
//		String secretKey = signConfig.getSecretKey();
//		if (StrUtil.isBlank(secretKey)) {
//			throw new SignatureException("服务端未配置签名密钥");
//		}
//
//		boolean valid = SM3SignatureUtils.verifySignature(params, secretKey);
//		if (!valid) {
//			throw new SignatureException("签名验证失败");
//		}
//	}
//
//	/**
//	 * 处理签名验证错误
//	 */
//	private void handleSignatureError(HttpServletResponse response, String message) throws IOException {
//		response.setStatus(HttpStatus.UNAUTHORIZED.value());
//		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//
//		R<?> result = R.failed(HttpStatus.UNAUTHORIZED.value(), message);
//		response.getWriter().write(JSONUtil.toJsonStr(result));
//	}
//}
//
