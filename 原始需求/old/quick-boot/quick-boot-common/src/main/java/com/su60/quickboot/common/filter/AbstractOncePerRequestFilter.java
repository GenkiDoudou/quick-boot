package com.su60.quickboot.common.filter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.su60.quickboot.common.core.R;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public abstract class AbstractOncePerRequestFilter extends OncePerRequestFilter {
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * 返回错误信息
	 *
	 * @param response 响应
	 * @param status   状态码
	 * @param message  错误信息
	 * @return
	 * @since 2026/2/4
	 */
	protected void returnError(HttpServletResponse response, Integer status, String message) throws IOException {
		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		R<?> result = R.failed(status, message);
		response.getWriter().write(JSONUtil.toJsonStr(result));
	}

	/**
	 * 是否在白名单
	 *
	 * @param url       url
	 * @param whiteList 白名单
	 * @return
	 * @since 2026/2/4
	 */
	protected boolean isWhiteRequest(String url, List<String> whiteList) {
		if (CollectionUtil.isEmpty(whiteList)) {
			return false;
		}
		return whiteList.stream().anyMatch(pattern -> pathMatcher.match(pattern, url));
	}
}
