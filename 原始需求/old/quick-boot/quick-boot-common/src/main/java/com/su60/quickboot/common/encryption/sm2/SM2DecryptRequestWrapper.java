//package com.su60.quickboot.common.encryption.sm2;
//
//import cn.hutool.core.io.IoUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.json.JSONUtil;
//import com.su60.quickboot.common.security.config.SecurityProperties;
//import jakarta.servlet.ReadListener;
//import jakarta.servlet.ServletInputStream;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletRequestWrapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.MediaType;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * SM2解密请求包装类
// *
// * @author luyanan
// * @since 2026/01/31
// */
//@Slf4j
//public class SM2DecryptRequestWrapper extends HttpServletRequestWrapper {
//
//	private final byte[] decryptedBody;
//	private final Map<String, String[]> decryptedParams;
//	private final SecurityProperties.Crypto cryptoConfig;
//
//	public SM2DecryptRequestWrapper(HttpServletRequest request, SecurityProperties.Crypto cryptoConfig) throws IOException {
//		super(request);
//		this.cryptoConfig = cryptoConfig;
//		this.decryptedParams = new HashMap<>();
//
//		// 解密URL参数
//		decryptUrlParams(request);
//
//		// 解密请求体
//		String contentType = request.getContentType();
//		if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
//			// JSON请求：解密Body
//			this.decryptedBody = decryptJsonBody(request);
//		} else {
//			// 表单请求：解密表单参数
//			decryptFormParams(request);
//			this.decryptedBody = null;
//		}
//	}
//
//	/**
//	 * 解密URL参数
//	 */
//	private void decryptUrlParams(HttpServletRequest request) {
//		Map<String, String[]> parameterMap = request.getParameterMap();
//		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
//			String key = entry.getKey();
//			String[] values = entry.getValue();
//
//			if (values != null && values.length > 0) {
//				String[] decryptedValues = new String[values.length];
//				for (int i = 0; i < values.length; i++) {
//					try {
//						// 解密参数值
//						decryptedValues[i] = SM2CryptoUtils.decrypt(values[i], cryptoConfig.getPrivateKey());
//
//						if (log.isDebugEnabled()) {
//							log.debug("[SM2解密] URL参数解密: key={}", key);
//						}
//					} catch (Exception e) {
//						log.warn("[SM2解密] URL参数解密失败: key={}, error={}", key, e.getMessage());
//						// 解密失败，使用原值
//						decryptedValues[i] = values[i];
//					}
//				}
//				decryptedParams.put(key, decryptedValues);
//			}
//		}
//	}
//
//	/**
//	 * 解密表单参数
//	 */
//	private void decryptFormParams(HttpServletRequest request) {
//		// 表单参数已经在decryptUrlParams中处理
//	}
//
//	/**
//	 * 解密JSON Body
//	 */
//	private byte[] decryptJsonBody(HttpServletRequest request) throws IOException {
//		String body = IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);
//
//		if (StrUtil.isBlank(body)) {
//			return new byte[0];
//		}
//
//		try {
//			// 解密Body
//			String decryptedBody = SM2CryptoUtils.decrypt(body, cryptoConfig.getPrivateKey());
//
//			if (log.isDebugEnabled()) {
//				log.debug("[SM2解密] Body解密成功，原始长度: {}, 解密后长度: {}", body.length(), decryptedBody.length());
//			}
//
//			return decryptedBody.getBytes(StandardCharsets.UTF_8);
//		} catch (Exception e) {
//			log.error("[SM2解密] Body解密失败", e);
//			throw new SM2CryptoException("请求数据解密失败: " + e.getMessage(), e);
//		}
//	}
//
//	@Override
//	public ServletInputStream getInputStream() throws IOException {
//		if (decryptedBody == null) {
//			return super.getInputStream();
//		}
//		return new CachedBodyServletInputStream(new ByteArrayInputStream(decryptedBody));
//	}
//
//	@Override
//	public BufferedReader getReader() throws IOException {
//		if (decryptedBody == null) {
//			return super.getReader();
//		}
//		return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
//	}
//
//	@Override
//	public String[] getParameterValues(String name) {
//		if (decryptedParams.containsKey(name)) {
//			return decryptedParams.get(name);
//		}
//		return super.getParameterValues(name);
//	}
//
//	@Override
//	public String getParameter(String name) {
//		String[] values = getParameterValues(name);
//		return (values != null && values.length > 0) ? values[0] : null;
//	}
//
//	@Override
//	public Map<String, String[]> getParameterMap() {
//		if (!decryptedParams.isEmpty()) {
//			return decryptedParams;
//		}
//		return super.getParameterMap();
//	}
//
//	/**
//	 * 自定义ServletInputStream
//	 */
//	private static class CachedBodyServletInputStream extends ServletInputStream {
//
//		private final ByteArrayInputStream inputStream;
//
//		public CachedBodyServletInputStream(ByteArrayInputStream inputStream) {
//			this.inputStream = inputStream;
//		}
//
//		@Override
//		public boolean isFinished() {
//			return inputStream.available() == 0;
//		}
//
//		@Override
//		public boolean isReady() {
//			return true;
//		}
//
//		@Override
//		public void setReadListener(ReadListener readListener) {
//			throw new UnsupportedOperationException();
//		}
//
//		@Override
//		public int read() throws IOException {
//			return inputStream.read();
//		}
//	}
//}
//
