//package com.su60.quickboot.common.security.sign;
//
//import cn.hutool.crypto.SmUtil;
//import org.junit.jupiter.api.Test;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * 签名工具类单元测试
// *
// * @author luyanan
// * @since 2026/01/31
// */
//class SM3SignatureUtilsTest {
//
//	private static final String SECRET_KEY = "test-secret-key-2026";
//
//	@Test
//	void testGenerateSignature() {
//		// 准备测试数据
//		Map<String, String> params = new HashMap<>();
//		params.put("username", "admin");
//		params.put("password", "123456");
//		params.put("timestamp", "1738310400");
//		params.put("nonce", "1738310400123456789");
//
//		// 生成签名
//		String signature = SM3SignatureUtils.generateSignature(params, SECRET_KEY);
//
//		// 验证签名不为空
//		assertNotNull(signature);
//		// 验证签名长度（SM3生成64位十六进制字符串）
//		assertEquals(64, signature.length());
//
//		System.out.println("生成的签名: " + signature);
//	}
//
//	@Test
//	void testVerifySignature() {
//		// 准备测试数据
//		Map<String, String> params = new HashMap<>();
//		params.put("username", "admin");
//		params.put("password", "123456");
//		params.put("timestamp", "1738310400");
//		params.put("nonce", "1738310400123456789");
//
//		// 生成签名
//		String signature = SM3SignatureUtils.generateSignature(params, SECRET_KEY);
//		params.put("sign", signature);
//
//		// 验证签名
//		boolean valid = SM3SignatureUtils.verifySignature(params, SECRET_KEY);
//		assertTrue(valid, "签名验证应该通过");
//	}
//
//	@Test
//	void testVerifySignatureWithWrongSign() {
//		// 准备测试数据
//		Map<String, String> params = new HashMap<>();
//		params.put("username", "admin");
//		params.put("password", "123456");
//		params.put("timestamp", "1738310400");
//		params.put("nonce", "1738310400123456789");
//		params.put("sign", "wrong_signature");
//
//		// 验证签名（应该失败）
//		boolean valid = SM3SignatureUtils.verifySignature(params, SECRET_KEY);
//		assertFalse(valid, "错误的签名应该验证失败");
//	}
//
//	@Test
//	void testVerifySignatureWithWrongKey() {
//		// 准备测试数据
//		Map<String, String> params = new HashMap<>();
//		params.put("username", "admin");
//		params.put("password", "123456");
//		params.put("timestamp", "1738310400");
//		params.put("nonce", "1738310400123456789");
//
//		// 使用正确的密钥生成签名
//		String signature = SM3SignatureUtils.generateSignature(params, SECRET_KEY);
//		params.put("sign", signature);
//
//		// 使用错误的密钥验证签名（应该失败）
//		boolean valid = SM3SignatureUtils.verifySignature(params, "wrong-secret-key");
//		assertFalse(valid, "使用错误的密钥应该验证失败");
//	}
//
//	@Test
//	void testIsTimestampValid() {
//		// 当前时间戳
//		long currentTimestamp = System.currentTimeMillis() / 1000;
//
//		// 测试有效的时间戳
//		assertTrue(SM3SignatureUtils.isTimestampValid(currentTimestamp, 300));
//
//		// 测试过期的时间戳（10分钟前）
//		long expiredTimestamp = currentTimestamp - 600;
//		assertFalse(SM3SignatureUtils.isTimestampValid(expiredTimestamp, 300));
//
//		// 测试未来的时间戳（10分钟后，超出有效期）
//		long futureTimestamp = currentTimestamp + 600;
//		assertFalse(SM3SignatureUtils.isTimestampValid(futureTimestamp, 300));
//	}
//
//	@Test
//	void testSignatureWithJsonParam() {
//		// 模拟POST JSON请求
//		String jsonBody = "{\"username\":\"admin\",\"password\":\"123456\"}";
//
//		Map<String, String> params = new HashMap<>();
//		params.put("timestamp", "1738310400");
//		params.put("nonce", "1738310400123456789");
//		params.put("json", jsonBody);
//
//		// 生成签名
//		String signature = SM3SignatureUtils.generateSignature(params, SECRET_KEY);
//		params.put("sign", signature);
//
//		// 验证签名
//		boolean valid = SM3SignatureUtils.verifySignature(params, SECRET_KEY);
//		assertTrue(valid, "JSON请求的签名验证应该通过");
//	}
//
//	@Test
//	void testSignatureHelper() {
//		// 测试GET请求签名
//		Map<String, String> getParams = new HashMap<>();
//		getParams.put("username", "admin");
//		getParams.put("pageNum", "1");
//		getParams.put("pageSize", "10");
//
//		Map<String, String> signedGetParams = SignatureHelper.signGetRequest(getParams, SECRET_KEY);
//
//		// 验证包含必要字段
//		assertTrue(signedGetParams.containsKey("timestamp"));
//		assertTrue(signedGetParams.containsKey("nonce"));
//		assertTrue(signedGetParams.containsKey("sign"));
//
//		// 验证签名
//		boolean valid = SM3SignatureUtils.verifySignature(signedGetParams, SECRET_KEY);
//		assertTrue(valid, "SignatureHelper生成的签名应该验证通过");
//
//		// 打印签名信息
//		SignatureHelper.printSignInfo(signedGetParams);
//	}
//
//	@Test
//	void testSignatureHelperForJson() {
//		// 测试POST JSON请求签名
//		String jsonBody = "{\"username\":\"admin\",\"password\":\"123456\"}";
//
//		Map<String, String> signHeaders = SignatureHelper.signJsonRequest(jsonBody, SECRET_KEY);
//
//		// 验证包含必要字段
//		assertTrue(signHeaders.containsKey("timestamp"));
//		assertTrue(signHeaders.containsKey("nonce"));
//		assertTrue(signHeaders.containsKey("sign"));
//		assertFalse(signHeaders.containsKey("json"), "json字段不应该在返回的Map中");
//
//		// 重新构建完整参数进行验证
//		Map<String, String> fullParams = new HashMap<>(signHeaders);
//		fullParams.put("json", jsonBody);
//
//		boolean valid = SM3SignatureUtils.verifySignature(fullParams, SECRET_KEY);
//		assertTrue(valid, "JSON请求的签名应该验证通过");
//
//		// 打印签名信息
//		SignatureHelper.printSignInfo(signHeaders);
//	}
//
//	@Test
//	void testSignatureConsistency() {
//		// 测试相同参数生成的签名是否一致
//		Map<String, String> params1 = new HashMap<>();
//		params1.put("username", "admin");
//		params1.put("timestamp", "1738310400");
//		params1.put("nonce", "1738310400123456789");
//
//		Map<String, String> params2 = new HashMap<>();
//		params2.put("username", "admin");
//		params2.put("timestamp", "1738310400");
//		params2.put("nonce", "1738310400123456789");
//
//		String sign1 = SM3SignatureUtils.generateSignature(params1, SECRET_KEY);
//		String sign2 = SM3SignatureUtils.generateSignature(params2, SECRET_KEY);
//
//		assertEquals(sign1, sign2, "相同参数应该生成相同的签名");
//	}
//
//	@Test
//	void testSignatureWithSpecialCharacters() {
//		// 测试包含特殊字符的参数
//		Map<String, String> params = new HashMap<>();
//		params.put("username", "admin@test.com");
//		params.put("password", "P@ssw0rd!#$%");
//		params.put("timestamp", "1738310400");
//		params.put("nonce", "1738310400123456789");
//
//		// 生成签名
//		String signature = SM3SignatureUtils.generateSignature(params, SECRET_KEY);
//		params.put("sign", signature);
//
//		// 验证签名
//		boolean valid = SM3SignatureUtils.verifySignature(params, SECRET_KEY);
//		assertTrue(valid, "包含特殊字符的参数签名验证应该通过");
//	}
//
//	@Test
//	void testSignatureWithEmptyValue() {
//		// 测试包含空值的参数
//		Map<String, String> params = new HashMap<>();
//		params.put("username", "admin");
//		params.put("password", "");
//		params.put("timestamp", "1738310400");
//		params.put("nonce", "1738310400123456789");
//
//		// 生成签名（空值应该被跳过）
//		String signature = SM3SignatureUtils.generateSignature(params, SECRET_KEY);
//		params.put("sign", signature);
//
//		// 验证签名
//		boolean valid = SM3SignatureUtils.verifySignature(params, SECRET_KEY);
//		assertTrue(valid, "包含空值的参数签名验证应该通过");
//	}
//
//	@Test
//	void testManualSM3Calculation() {
//		// 手动计算SM3签名，验证算法正确性
//		String signContent = "nonce=1738310400123456789&password=123456&timestamp=1738310400&username=admin&key=" + SECRET_KEY;
//		String expectedSign = SmUtil.sm3(signContent);
//
//		// 使用工具类生成签名
//		Map<String, String> params = new HashMap<>();
//		params.put("username", "admin");
//		params.put("password", "123456");
//		params.put("timestamp", "1738310400");
//		params.put("nonce", "1738310400123456789");
//
//		String actualSign = SM3SignatureUtils.generateSignature(params, SECRET_KEY);
//
//		assertEquals(expectedSign, actualSign, "手动计算的签名应该与工具类生成的签名一致");
//
//		System.out.println("待签名字符串: " + signContent);
//		System.out.println("生成的签名: " + actualSign);
//	}
//}
//
