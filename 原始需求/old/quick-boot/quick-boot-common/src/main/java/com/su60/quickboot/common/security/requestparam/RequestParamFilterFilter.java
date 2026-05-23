package com.su60.quickboot.common.security.requestparam;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;
import com.su60.quickboot.common.core.R;
import com.su60.quickboot.common.exception.ErrorException;
import com.su60.quickboot.common.security.config.SecurityProperties;
import com.su60.quickboot.common.security.sql.SqlInjectUtils;
import com.su60.quickboot.common.security.sql.SqlKeywordsProvider;
import com.su60.quickboot.common.sensitive.SensitiveWordService;
import com.su60.quickboot.common.sensitive.SensitiveWordStrategy;
import com.su60.quickboot.common.utils.ServletUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE+1000)
public class RequestParamFilterFilter extends OncePerRequestFilter {

	private final SecurityProperties securityProperties;
	private final SqlKeywordsProvider sqlKeywordsProvider;
	private final SensitiveWordService sensitiveWordService;

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {


		// SQL 注入
		SecurityProperties.SqlInjectProperties sqlInject = securityProperties.getSqlInject();
		boolean sqlInjectEnabled = Boolean.TRUE.equals(sqlInject.getEnabled())
				&& !ignore(request, sqlInject.getIgnoreUrls());

		// XSS
		SecurityProperties.XssProperties xss = securityProperties.getXss();
		boolean xssEnabled = Boolean.TRUE.equals(xss.getEnabled())
				&& !ignore(request, xss.getIgnoreUrls());

		// 敏感词
		SecurityProperties.SensitiveWordProperties sensitiveWord = securityProperties.getSensitiveWord();
		boolean sensitiveWordEnabled = Boolean.TRUE.equals(sensitiveWord.getEnabled())
				&& !ignore(request, sensitiveWord.getIgnoreUrls());

		// 都没启用，直接放行
		if (!sqlInjectEnabled && !xssEnabled && !sensitiveWordEnabled) {
			filterChain.doFilter(request, response);
			return;
		}

		// 构建统一过滤函数
		Function<String, String> filterFunction = value -> {
			if (StrUtil.isBlank(value)) {
				return value;
			}

			// 1️⃣ SQL 注入检测（只检测，不修改）
			if (sqlInjectEnabled) {
				List<String> hits = SqlInjectUtils.detect(value, sqlKeywordsProvider.getKeywords());
				if (CollUtil.isNotEmpty(hits)) {
					log.warn("SQL注入拦截: url={}, ip={}, param={}, keywords={}",
							request.getRequestURI(),
							ServletUtil.getClientIP(request),
							value,
							hits);
					throw new ErrorException("请求参数包含非法字符:" + value);
				}
			}

			String result = value;

			// 2️⃣ XSS 过滤
			if (xssEnabled) {
				result = HtmlUtil.cleanHtmlTag(result).trim();
			}

			// 3️⃣ 敏感词处理
			if (sensitiveWordEnabled) {
				String hitWord = sensitiveWordService.getText(result);
				if (StrUtil.isNotBlank(hitWord)) {
					if (sensitiveWord.getStrategy() == SensitiveWordStrategy.THROW) {
						throw new ErrorException("请求参数包含敏感词:" + value);

					}
					result = sensitiveWordService.replace(result);
				}
			}

			return result;
		};

		// 包装 Request（方案二：缓存 Body）
		RequestParamFilterHttpServletRequestWrapper wrapper =
				null;
		try {
			wrapper = new RequestParamFilterHttpServletRequestWrapper(request, filterFunction);
		} catch (Exception e) {
//			throw new RuntimeException(e);
			writeErrorResponse(response, e.getLocalizedMessage());
			return;
		}

//		filterChain.doFilter(request,response);
		filterChain.doFilter(wrapper, response);
	}

	private void writeErrorResponse(HttpServletResponse response, String errorMag) throws IOException {
		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(JSONUtil.toJsonStr(R.failed(HttpStatus.BAD_REQUEST.value(), errorMag)));
	}

	/**
	 * 是否忽略路径
	 */
	private boolean ignore(HttpServletRequest request, List<String> ignores) {
		if (ignores == null || ignores.isEmpty()) {
			return false;
		}
		String path = request.getRequestURI();
		return ignores.stream().anyMatch(p -> pathMatcher.match(p, path));
	}
}
