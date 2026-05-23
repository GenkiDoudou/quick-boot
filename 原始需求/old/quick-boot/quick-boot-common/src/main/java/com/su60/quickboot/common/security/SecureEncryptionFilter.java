package com.su60.quickboot.common.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.su60.quickboot.common.core.R;
import com.su60.quickboot.common.encryption.sm2.SM2EncryptResponseWrapper;
import com.su60.quickboot.common.filter.AbstractOncePerRequestFilter;
import com.su60.quickboot.common.security.config.SecurityProperties;
import com.su60.quickboot.common.security.sign.CachedBodyHttpServletRequest;
import com.su60.quickboot.common.security.sign.SM3SignatureUtils;
import com.su60.quickboot.common.utils.ServletUtil;
import com.su60.quickboot.common.utils.SmUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "security.crypto", name = "enabled", havingValue = "true")
public class SecureEncryptionFilter extends AbstractOncePerRequestFilter {

	private final SecurityProperties securityProperties;


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


		try {
			SecurityProperties.Crypto crypto = securityProperties.getCrypto();

			if (null == crypto || !crypto.getEnabled()) {
				filterChain.doFilter(request, response);
				return;
			}
			if (isWhiteRequest(request.getRequestURI(), crypto.getRequest().getIpWhitelist())) {
				filterChain.doFilter(request, response);
				return;
			}

			Object oauthClient = request.getAttribute("oauthClient");
			if (null == oauthClient) {
				R.failed(response, 401, "客户端配置不能为空");
				return;
			}
			OauthClientVo oauthClientVo = (OauthClientVo) oauthClient;
			if (isEnableRequestDecrypt(request.getRequestURI(), crypto)) {
				// 参数解密
				String key = request.getParameter("key");
				if (StrUtil.isBlank(key)) {
					returnError(response, 400, "参数key不能为空");
					return;
				}
				// 私钥解密
				String decryptSM2 = SmUtils.decryptSM2(key, oauthClientVo.getPrivateKey());
				String sm4Key = decryptSM2.split("&")[0];
				String iv = decryptSM2.split("&")[1];
				DecryptRequestWrapper wrapper = new DecryptRequestWrapper(request, (enc) -> SmUtils.decryptSm4(enc, sm4Key, iv));
				request = wrapper;
			}


			// 签名验签
			if (isEnableSign(request.getRequestURI(), securityProperties)) {

				// 开启签名
				String sign = request.getHeader("sign");
				String nonce = request.getHeader("nonce");
				if (StrUtil.isBlank(sign) || StrUtil.isBlank(nonce)) {
					returnError(response, 400, "签名参数不正确");
					return;
				}
				// 解析nonce
				Long timestamp = SecurityNonceUtil.parse(nonce, oauthClientVo.getClientSecret());
				if (timestamp < System.currentTimeMillis() - 1000 * 60 * 5) {
					returnError(response, 400, "时间戳已过期");
					return;
				}
				//
				Map<String, String> signParams = new HashMap<>();
				collectGetParams(request.getParameterMap(), signParams);
				if (request instanceof DecryptRequestWrapper) {
					DecryptRequestWrapper decryptRequestWrapper = (DecryptRequestWrapper) request;
					byte[] body = decryptRequestWrapper.decryptedBody;
					if (body != null) {
						String bodyStr = new String(body, StandardCharsets.UTF_8);
						collectJsonParams(bodyStr, signParams);
					}
				} else {
					ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
					collectJsonParams(requestWrapper.getContentAsString(), signParams);
				}

				signParams.remove("key");
				boolean valid = SM3SignatureUtils.verifySignature(signParams, nonce, sign);
				if (!valid) {
					returnError(response, 400, "签名验证失败");
				}
			}

			if (isEnableResponseEncrypt(request.getRequestURI(), crypto, response)) {
				// 结果加密
				String respSm4Key = SmUtils.genKey();
				String respIv = SmUtils.genKey();
				String resKey = SmUtils.encryptSM2(respSm4Key + "&" + respIv, oauthClientVo.getPublicKey());
				SM2EncryptResponseWrapper sm2EncryptResponseWrapper = new SM2EncryptResponseWrapper(response);
				filterChain.doFilter(request, sm2EncryptResponseWrapper);
				// 文件下载请求也不处理
				String contentDisposition = response.getHeader("Content-Disposition");
				boolean isDownload = contentDisposition != null &&
						contentDisposition.contains("attachment");
				sm2EncryptResponseWrapper.encryptAndWrite((enc) -> SmUtils.encryptSM4(enc, respSm4Key, respIv), resKey, !isDownload);
			} else {
				filterChain.doFilter(request, response);
			}


		} catch (Exception e) {
//			throw new RuntimeException(e);
			e.printStackTrace();
			filterChain.doFilter(request, response);
		}
	}

	/**
	 * 是否开启请求加密
	 *
	 * @param url
	 * @return
	 * @since 2026/2/5
	 */
	private boolean isEnableRequestDecrypt(String url, SecurityProperties.Crypto cryptoProperties) {
		if (!cryptoProperties.getEnabled()) {
			return false;
		}
		return !isWhiteRequest(url, cryptoProperties.getRequest().getIpWhitelist());
	}


	private boolean isEnableSign(String url, SecurityProperties securityProperties) {
		if (!securityProperties.getSign().getEnabled()) {
			return false;
		}
		return !isWhiteRequest(url, securityProperties.getSign().getIpWhitelist());
	}


	/**
	 * 是否开启 响应内容加密
	 *
	 * @param url
	 * @param cryptoProperties
	 * @return
	 * @since 2026/2/5
	 */
	private boolean isEnableResponseEncrypt(String url, SecurityProperties.Crypto cryptoProperties, HttpServletResponse response) {
		if (!cryptoProperties.getResponse().getEnable()) {
			return false;
		}


		return !isWhiteRequest(url, cryptoProperties.getResponse().getIpWhitelist());
	}


	/**
	 * 收集GET请求参数（不包括签名字段）
	 */
	private void collectGetParams(Map<String, String[]> parameterMap, Map<String, String> params) {
		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
			String key = entry.getKey();
			String[] values = entry.getValue();

			// 跳过签名字段（这些字段已经从Header中获取）
			if ("sign".equals(key) || "nonce".equals(key)) {
				continue;
			}

			if (values != null && values.length > 0) {
				// 只取第一个值
				params.put(key, values[0]);
			}
		}
	}


	/**
	 * 收集POST JSON参数
	 */
	private void collectJsonParams(String body, Map<String, String> params) throws IOException {
		if (StrUtil.isNotBlank(body)) {
			// 将JSON字符串作为json参数
			params.put("json", body);
			if (log.isDebugEnabled()) {
				log.debug("[签名验证] JSON请求体: {}", body);
			}
		}
	}
}
