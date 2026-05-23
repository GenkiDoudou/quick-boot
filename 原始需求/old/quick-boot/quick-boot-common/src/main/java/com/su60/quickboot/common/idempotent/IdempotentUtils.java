package com.su60.quickboot.common.idempotent;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.su60.quickboot.common.utils.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 幂等工具类
 * <p>
 * 用于生成幂等标识
 * </p>
 *
 * @author luyanan
 * @since 2026/01/18
 */
public class IdempotentUtils {

	/**
	 * 默认的幂等标识请求头名称
	 */
	public static final String DEFAULT_IDEMPOTENT_HEADER = "X-Request-Id";

	/**
	 * 默认的幂等标识参数名称
	 */
	public static final String DEFAULT_IDEMPOTENT_PARAM = "requestId";

	/**
	 * 生成幂等标识
	 * <p>
	 * 优先级：
	 * 1. 从请求头 X-Request-Id 获取
	 * 2. 从请求参数 requestId 获取
	 * 3. 根据请求路径和参数生成MD5
	 * </p>
	 *
	 * @param request HTTP请求
	 * @return 幂等标识
	 */
	public static String generateIdempotentKey(HttpServletRequest request) {
		// 1. 从请求头获取
		String requestId = request.getHeader(DEFAULT_IDEMPOTENT_HEADER);
		if (StrUtil.isNotBlank(requestId)) {
			return requestId;
		}

		// 2. 从请求参数获取
		requestId = request.getParameter(DEFAULT_IDEMPOTENT_PARAM);
		if (StrUtil.isNotBlank(requestId)) {
			return requestId;
		}

		// 3. 根据请求路径和参数生成
		return generateKeyByRequest(request);
	}

	/**
	 * 根据请求路径和参数生成MD5标识
	 *
	 * @param request HTTP请求
	 * @return MD5标识
	 */
	public static String generateKeyByRequest(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getRequestURI());
		sb.append("?");

		// 获取所有参数并排序
		Map<String, String> params = ServletUtil.getParamMap(request);
		String paramStr = params.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining("&"));

		sb.append(paramStr);

		// 获取请求体（如果是POST/PUT等）
		if (ServletUtil.isPostMethod(request) || "PUT".equalsIgnoreCase(request.getMethod()) || "PATCH".equalsIgnoreCase(request.getMethod())) {
			try {
				String body = ServletUtil.getBody(request);
				if (StrUtil.isNotBlank(body)) {
					sb.append("&body=").append(body);
				}
			} catch (Exception e) {
				// 忽略读取请求体的异常
			}
		}

		// 生成MD5
		return DigestUtil.md5Hex(sb.toString());
	}

	/**
	 * 从SpEL表达式中解析幂等标识
	 * <p>
	 * 如果表达式为空，则使用默认方式生成
	 * </p>
	 *
	 * @param keyExpression SpEL表达式
	 * @param request       HTTP请求
	 * @return 幂等标识
	 */
	public static String parseKeyFromExpression(String keyExpression, HttpServletRequest request) {
		if (StrUtil.isBlank(keyExpression)) {
			return generateIdempotentKey(request);
		}

		// 如果表达式是简单的参数名，尝试从请求中获取
		if (keyExpression.startsWith("#")) {
			String paramName = keyExpression.substring(1);
			String value = request.getParameter(paramName);
			if (StrUtil.isNotBlank(value)) {
				return value;
			}
			// 尝试从请求头获取
			value = request.getHeader(paramName);
			if (StrUtil.isNotBlank(value)) {
				return value;
			}
		}

		// 如果无法解析，使用默认方式
		return generateIdempotentKey(request);
	}

	/**
	 * 获取当前HTTP请求
	 *
	 * @return HttpServletRequest
	 */
	public static HttpServletRequest getCurrentRequest() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes != null) {
			return attributes.getRequest();
		}
		return null;
	}
}
