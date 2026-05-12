package com.su60.quickboot.common.encryption.sm2;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.su60.quickboot.common.security.config.SecurityProperties;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * SM2加密响应包装类
 *
 * @author luyanan
 * @since 2026/01/31
 */
@Slf4j
public class SM2EncryptResponseWrapper extends HttpServletResponseWrapper {

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final ServletOutputStream servletOutputStream;
	private final PrintWriter writer;

	public SM2EncryptResponseWrapper(HttpServletResponse response) throws IOException {
		super(response);
		this.servletOutputStream = new CachedBodyServletOutputStream(outputStream);
		this.writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return servletOutputStream;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return writer;
	}

	/**
	 * 加密并写入响应数据
	 */
	public void encryptAndWrite(Function<String, String> function, String resKey,boolean encryption) throws IOException {
		// 刷新缓冲区
		writer.flush();
		outputStream.flush();

		// 获取原始响应数据
		byte[] originalData = outputStream.toByteArray();

		if (originalData.length == 0) {
			return;
		}
		if (!encryption){
			getResponse().getOutputStream().write(originalData);
			getResponse().getOutputStream().flush();
		}

		String originalContent = new String(originalData, StandardCharsets.UTF_8);

		try {
			// 加密响应数据
			String encrypted = function.apply(originalContent);
			Map<String, Object> res = new HashMap<>();
			res.put("_enc", encrypted);
			res.put("_key", resKey);

			String encryptedContent = JSONUtil.toJsonStr(res);


			// 写入加密后的数据
			byte[] encryptedData = encryptedContent.getBytes(StandardCharsets.UTF_8);
			getResponse().setContentLength(encryptedData.length);
			getResponse().getOutputStream().write(encryptedData);
			getResponse().getOutputStream().flush();

		} catch (Exception e) {
			log.error("[SM2加密] 响应加密失败", e);
			// 加密失败，返回原始数据
			getResponse().getOutputStream().write(originalData);
			getResponse().getOutputStream().flush();
		}
	}

	/**
	 * 自定义ServletOutputStream
	 */
	private static class CachedBodyServletOutputStream extends ServletOutputStream {

		private final ByteArrayOutputStream outputStream;

		public CachedBodyServletOutputStream(ByteArrayOutputStream outputStream) {
			this.outputStream = outputStream;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void write(int b) throws IOException {
			outputStream.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			outputStream.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			outputStream.write(b, off, len);
		}
	}
}

