package com.su60.quickboot.common.security;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DecryptRequestWrapper extends HttpServletRequestWrapper {
	public final byte[] decryptedBody;

	private Map<String, String[]> decryptedParams = new HashMap<>();

	public DecryptRequestWrapper(HttpServletRequest request, Function<String, String> function) throws IOException {
		super(request);


		String contentType = request.getContentType();

		if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
			// JSON请求：解密Body
			this.decryptedBody = decryptJsonBody(request, function);
		} else {
			// 表单请求：解密表单参数
			decryptFormParams(request, function);
			this.decryptedBody = null;
		}
	}

	private byte[] decryptJsonBody(HttpServletRequest request, Function<String, String> function) throws IOException {
		String body = IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);

		if (StrUtil.isBlank(body)) {
			return new byte[0];
		}
		if (JSONUtil.isTypeJSONObject(body)) {
			JSONObject json = JSONUtil.parseObj(body);
			String enc = json.getStr("_enc");
			if (StrUtil.isBlank(enc)) {
				return body.getBytes(StandardCharsets.UTF_8);
			} else {
				String apply = function.apply(enc);
				return apply.getBytes(StandardCharsets.UTF_8);
			}
		}
		return body.getBytes(StandardCharsets.UTF_8);
	}

	private void decryptFormParams(HttpServletRequest request, Function<String, String> function) {
		String enc = request.getParameter("_enc");
		if (StrUtil.isBlank(enc)) {
			return;
		}
		String decryptedJson = function.apply(enc);
		// 1. 先复制原始参数（可选：保留未加密参数）
		Map<String, String[]> originalParams = request.getParameterMap();
		Map<String, String[]> newParams = new HashMap<>();

		// 2. 如果有解密内容，解析并合并
		if (decryptedJson != null && JSONUtil.isTypeJSONObject(decryptedJson)) {
			JSONObject json = JSONUtil.parseObj(decryptedJson);
			for (Map.Entry<String, Object> entry : json.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue() == null ? "" : entry.getValue().toString();
				newParams.put(key, new String[]{value});
			}
		}

		// 3. （可选）保留原始参数（如 token、timestamp 等非加密字段）
		//    如果你希望只用解密参数，可跳过此步
		for (Map.Entry<String, String[]> entry : originalParams.entrySet()) {
			// 避免覆盖解密参数（优先使用解密值）
			if (!newParams.containsKey(entry.getKey())) {
				newParams.put(entry.getKey(), entry.getValue());
			}
		}
		newParams.remove("key");
		newParams.remove("_enc");
		this.decryptedParams = Collections.unmodifiableMap(newParams);

	}

	@Override
	public String getParameter(String name) {
		String[] values = getParameterValues(name);
		return values != null && values.length > 0 ? values[0] : null;
	}

	@Override
	public String[] getParameterValues(String name) {
		return decryptedParams.get(name);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return decryptedParams;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(decryptedParams.keySet());
	}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			if (decryptedBody == null) {
				return super.getInputStream();
			}
			return new CachedBodyServletInputStream(new ByteArrayInputStream(decryptedBody));
		}

	@Override
	public BufferedReader getReader() throws IOException {
		if (decryptedBody == null) {
			return super.getReader();
		}
		return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
	}


	/**
	 * 自定义ServletInputStream
	 */
	private static class CachedBodyServletInputStream extends ServletInputStream {

		private final ByteArrayInputStream inputStream;

		public CachedBodyServletInputStream(ByteArrayInputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Override
		public boolean isFinished() {
			return inputStream.available() == 0;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setReadListener(ReadListener readListener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int read() throws IOException {
			return inputStream.read();
		}
	}
}