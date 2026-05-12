package com.su60.quickboot.common.security.requestparam;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.utils.JsonTraverseUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * 请求参数安全过滤 Wrapper
 * <p>
 * 方案二：
 * 1. 构造时一次性读取 Body
 * 2. JSON 文本字段统一处理
 * 3. Body 可重复读取
 *
 * @author luyanan
 * @since 2026/1/16
 */
@Slf4j
public class RequestParamFilterHttpServletRequestWrapper extends HttpServletRequestWrapper {

	/**
	 * 处理函数（XSS / 敏感词 / 替换等）
	 */
	private final Function<String, String> filterFunction;

	/**
	 * 缓存后的 body
	 */
	private byte[] cachedBody;

	public RequestParamFilterHttpServletRequestWrapper(HttpServletRequest request,
													   Function<String, String> filterFunction) throws Exception {
		super(request);
		this.filterFunction = filterFunction;
		this.cachedBody = readAndFilterBody(request);
	}

	/* ======================= 参数处理（query / form） ======================= */

	@Override
	public String getParameter(String name) {
		String value = super.getParameter(name);
		if (value == null || filterFunction == null) {
			return value;
		}
		return filterFunction.apply(value);
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] values = super.getParameterValues(name);
		if (values == null || filterFunction == null) {
			return values;
		}
		String[] filtered = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			filtered[i] = filterFunction.apply(values[i]);
		}
		return filtered;
	}

	/* ======================= Body 处理 ======================= */

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream bais = new ByteArrayInputStream(this.cachedBody);
		return new ServletInputStream() {
			@Override
			public int read() {
				return bais.read();
			}

			@Override
			public boolean isFinished() {
				return bais.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener listener) {
			}
		};
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(
				new InputStreamReader(getInputStream(), StandardCharsets.UTF_8)
		);
	}

	/* ======================= 内部方法 ======================= */

	/**
	 * 读取并处理 body
	 */
	private byte[] readAndFilterBody(HttpServletRequest request) throws Exception {
		if (!isJsonRequest() || filterFunction == null) {
			return IoUtil.readBytes(request.getInputStream());
		}

		String json = IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);
		if (StrUtil.isBlank(json)) {
			return json.getBytes(StandardCharsets.UTF_8);
		}

		try {
			json = JsonTraverseUtil.traverse(json, (node, path) -> {
				if (node.isTextual()) {
					return filterFunction.apply(node.asText());
				}
				return node.asText();
			});
		} catch (Exception e) {
//			log.error("JSON 参数解析失败，返回原始内容", e);
			throw e;
		}

		return json.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * 是否 JSON 请求
	 */
	private boolean isJsonRequest() {
		String contentType = super.getHeader(HttpHeaders.CONTENT_TYPE);
		return contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);
	}
}
