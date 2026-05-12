package com.su60.quickboot.common.security.sign;

import java.util.HashMap;
import java.util.Map;

/**
 * 签名辅助工具类（用于客户端生成签名）
 *
 * @author luyanan
 * @since 2026/01/31
 */
public class SignatureHelper {

	/**
	 * 为GET请求生成签名
	 *
	 * @param params    请求参数
	 * @param secretKey 密钥
	 * @return 包含签名的完整参数Map
	 */
	public static Map<String, String> signGetRequest(Map<String, String> params, String secretKey) {
		Map<String, String> signParams = new HashMap<>(params);
		
		// 添加时间戳和随机字符串
		long timestamp = System.currentTimeMillis() / 1000;
		String nonce = generateNonce();
		
		signParams.put("timestamp", String.valueOf(timestamp));
		signParams.put("nonce", nonce);
		
		// 生成签名
		String signature = SM3SignatureUtils.generateSignature(signParams, secretKey);
		signParams.put("sign", signature);
		
		return signParams;
	}

	/**
	 * 为POST表单请求生成签名
	 *
	 * @param params    表单参数
	 * @param secretKey 密钥
	 * @return 包含签名的完整参数Map
	 */
	public static Map<String, String> signFormRequest(Map<String, String> params, String secretKey) {
		return signGetRequest(params, secretKey);
	}

	/**
	 * 为POST JSON请求生成签名
	 *
	 * @param jsonBody  JSON字符串
	 * @param secretKey 密钥
	 * @return 包含签名信息的Map（用于放入Header或参数）
	 */
	public static Map<String, String> signJsonRequest(String jsonBody, String secretKey) {
		Map<String, String> signParams = new HashMap<>();
		
		// 添加时间戳和随机字符串
		long timestamp = System.currentTimeMillis() / 1000;
		String nonce = generateNonce();
		
		signParams.put("timestamp", String.valueOf(timestamp));
		signParams.put("nonce", nonce);
		signParams.put("json", jsonBody);
		
		// 生成签名
		String signature = SM3SignatureUtils.generateSignature(signParams, secretKey);
		signParams.put("sign", signature);
		
		// 移除json字段（不需要传递）
		signParams.remove("json");
		
		return signParams;
	}

	/**
	 * 生成随机字符串（Nonce）
	 *
	 * @return 随机字符串
	 */
	private static String generateNonce() {
		return String.valueOf(System.nanoTime());
	}

	/**
	 * 打印签名信息（用于调试）
	 *
	 * @param signParams 签名参数
	 */
	public static void printSignInfo(Map<String, String> signParams) {
		System.out.println("========== 签名信息 ==========");
		System.out.println("timestamp: " + signParams.get("timestamp"));
		System.out.println("nonce: " + signParams.get("nonce"));
		System.out.println("sign: " + signParams.get("sign"));
		System.out.println("==============================");
	}
}

