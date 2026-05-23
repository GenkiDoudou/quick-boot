//package com.su60.quickboot.common.encryption.sm2;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * SM2加密解密工具类测试
// *
// * @author luyanan
// * @since 2026/01/31
// */
//class SM2CryptoUtilsTest {
//
//	@Test
//	void testGenerateKeyPair() {
//		// 生成密钥对
//		String[] keyPair = SM2CryptoUtils.generateKeyPair();
//
//		assertNotNull(keyPair);
//		assertEquals(2, keyPair.length);
//		assertNotNull(keyPair[0]); // 私钥
//		assertNotNull(keyPair[1]); // 公钥
//
//		System.out.println("私钥: " + keyPair[0]);
//		System.out.println("公钥: " + keyPair[1]);
//	}
//
//	@Test
//	void testEncryptAndDecrypt() {
//		// 生成密钥对
//		String[] keyPair = SM2CryptoUtils.generateKeyPair();
//		String privateKey = keyPair[0];
//		String publicKey = keyPair[1];
//
//		// 测试数据
//		String originalData = "Hello, SM2 Crypto!";
//
//		// 加密
//		String encrypted = SM2CryptoUtils.encrypt(originalData, publicKey);
//		assertNotNull(encrypted);
//		assertNotEquals(originalData, encrypted);
//
//		System.out.println("原始数据: " + originalData);
//		System.out.println("加密后: " + encrypted);
//
//		// 解密
//		String decrypted = SM2CryptoUtils.decrypt(encrypted, privateKey);
//		assertNotNull(decrypted);
//		assertEquals(originalData, decrypted);
//
//		System.out.println("解密后: " + decrypted);
//	}
//
//	@Test
//	void testEncryptChineseText() {
//		// 生成密钥对
//		String[] keyPair = SM2CryptoUtils.generateKeyPair();
//		String privateKey = keyPair[0];
//		String publicKey = keyPair[1];
//
//		// 测试中文数据
//		String originalData = "你好，国密SM2加密！";
//
//		// 加密
//		String encrypted = SM2CryptoUtils.encrypt(originalData, publicKey);
//		assertNotNull(encrypted);
//
//		// 解密
//		String decrypted = SM2CryptoUtils.decrypt(encrypted, privateKey);
//		assertEquals(originalData, decrypted);
//
//		System.out.println("中文原始数据: " + originalData);
//		System.out.println("解密后: " + decrypted);
//	}
//
//	@Test
//	void testEncryptLongText() {
//		// 生成密钥对
//		String[] keyPair = SM2CryptoUtils.generateKeyPair();
//		String privateKey = keyPair[0];
//		String publicKey = keyPair[1];
//
//		// 测试长文本
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < 100; i++) {
//			sb.append("这是一段测试文本，用于测试SM2加密长文本的能力。");
//		}
//		String originalData = sb.toString();
//
//		// 加密
//		String encrypted = SM2CryptoUtils.encrypt(originalData, publicKey);
//		assertNotNull(encrypted);
//
//		// 解密
//		String decrypted = SM2CryptoUtils.decrypt(encrypted, privateKey);
//		assertEquals(originalData, decrypted);
//
//		System.out.println("长文本长度: " + originalData.length());
//		System.out.println("加密后长度: " + encrypted.length());
//	}
//
//	@Test
//	void testVerifyKeyPair() {
//		// 生成密钥对
//		String[] keyPair = SM2CryptoUtils.generateKeyPair();
//		String privateKey = keyPair[0];
//		String publicKey = keyPair[1];
//
//		// 验证密钥对
//		boolean valid = SM2CryptoUtils.verifyKeyPair(privateKey, publicKey);
//		assertTrue(valid);
//
//		// 测试错误的密钥对
//		String[] anotherKeyPair = SM2CryptoUtils.generateKeyPair();
//		boolean invalid = SM2CryptoUtils.verifyKeyPair(privateKey, anotherKeyPair[1]);
//		assertFalse(invalid);
//	}
//
//	@Test
//	void testEncryptEmptyString() {
//		// 生成密钥对
//		String[] keyPair = SM2CryptoUtils.generateKeyPair();
//		String publicKey = keyPair[1];
//
//		// 测试空字符串
//		String encrypted = SM2CryptoUtils.encrypt("", publicKey);
//		assertEquals("", encrypted);
//
//		// 测试null
//		String encryptedNull = SM2CryptoUtils.encrypt(null, publicKey);
//		assertNull(encryptedNull);
//	}
//
//	@Test
//	void testEncryptWithInvalidKey() {
//		// 测试无效的公钥
//		assertThrows(Exception.class, () -> {
//			SM2CryptoUtils.encrypt("test", "invalid-key");
//		});
//	}
//}
//
