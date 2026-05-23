package com.su60.quickboot.common.security.sign;

import cn.hutool.core.io.IoUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 可重复读取Body的HttpServletRequest包装类
 *
 * @author luyanan
 * @since 2026/01/31
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

	private final byte[] cachedBody;

	public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
		super(request);
		// 缓存请求体
		this.cachedBody = IoUtil.readBytes(request.getInputStream());
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new CachedBodyServletInputStream(this.cachedBody);
	}

	@Override
	public BufferedReader getReader() throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
		return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
	}

	/**
	 * 获取缓存的Body内容
	 *
	 * @return Body字符串
	 */
	public String getBody() {
		return new String(this.cachedBody, StandardCharsets.UTF_8);
	}

	/**
	 * 自定义ServletInputStream
	 */
	private static class CachedBodyServletInputStream extends ServletInputStream {

		private final ByteArrayInputStream inputStream;

		public CachedBodyServletInputStream(byte[] cachedBody) {
			this.inputStream = new ByteArrayInputStream(cachedBody);
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

