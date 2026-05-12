package com.su60.quickboot.common.encryption.sm2;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 国密SM2加密解密工具类
 *
 * @author luyanan
 * @since 2026/01/31
 */
@Slf4j
public class SM2CryptoUtils {

	/**
	 * 使用公钥加密
	 *
	 * @param data      待加密数据
	 * @param publicKey 公钥（Base64编码字符串）
	 * @return 加密后的数据（Base64编码字符串）
	 */
	public static String encrypt(String data, String publicKey) {
		if (StrUtil.isBlank(data)) {
			return data;
		}
		if (StrUtil.isBlank(publicKey)) {
			throw new IllegalArgumentException("公钥不能为空");
		}

		try {
			// 创建SM2实例（使用Base64格式的公钥）
			SM2 sm2 = new SM2(null, publicKey);
			
			// 加密（使用公钥）
			byte[] encrypted = sm2.encrypt(data.getBytes(StandardCharsets.UTF_8), KeyType.PublicKey);
			
			// 转换为Base64字符串
			String result = cn.hutool.core.codec.Base64.encode(encrypted);
			
			if (log.isDebugEnabled()) {
				log.debug("[SM2加密] 原始数据长度: {}, 加密后长度: {}", data.length(), result.length());
			}
			
			return result;
		} catch (Exception e) {
			log.error("[SM2加密] 加密失败，公钥格式: {}", publicKey.substring(0, Math.min(20, publicKey.length())) + "...", e);
			throw new RuntimeException("SM2加密失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 使用私钥解密
	 *
	 * @param encryptedData 加密数据（Base64编码字符串）
	 * @param privateKey    私钥（Base64编码字符串）
	 * @return 解密后的数据
	 */
	public static String decrypt(String encryptedData, String privateKey) {
		if (StrUtil.isBlank(encryptedData)) {
			return encryptedData;
		}
		if (StrUtil.isBlank(privateKey)) {
			throw new IllegalArgumentException("私钥不能为空");
		}

		try {
			// 创建SM2实例（使用Base64格式的私钥）
			SM2 sm2 = new SM2(privateKey, null);
			
			// Base64解码
			byte[] encryptedBytes = cn.hutool.core.codec.Base64.decode(encryptedData);
			
			// 解密（使用私钥）
			byte[] decrypted = sm2.decrypt(encryptedBytes, KeyType.PrivateKey);
			
			String result = new String(decrypted, StandardCharsets.UTF_8);
			
			if (log.isDebugEnabled()) {
				log.debug("[SM2解密] 加密数据长度: {}, 解密后长度: {}", encryptedData.length(), result.length());
			}
			
			return result;
		} catch (Exception e) {
			log.error("[SM2解密] 解密失败", e);
			throw new RuntimeException("SM2解密失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 生成SM2密钥对
	 *
	 * @return 密钥对 [privateKey, publicKey]
	 */
	public static String[] generateKeyPair() {
		SM2 sm2 = SmUtil.sm2();
		String privateKey = sm2.getPrivateKeyBase64();
		String publicKey = sm2.getPublicKeyBase64();
		
		if (log.isDebugEnabled()) {
			log.debug("[SM2密钥] 生成密钥对成功");
			log.debug("[SM2密钥] 私钥: {}", privateKey);
			log.debug("[SM2密钥] 公钥: {}", publicKey);
		}
		
		return new String[]{privateKey, publicKey};
	}

	/**
	 * 验证密钥对是否匹配
	 *
	 * @param privateKey 私钥
	 * @param publicKey  公钥
	 * @return 是否匹配
	 */
	public static boolean verifyKeyPair(String privateKey, String publicKey) {
		try {
			String testData = "test-data-for-verification";
			String encrypted = encrypt(testData, publicKey);
			String decrypted = decrypt(encrypted, privateKey);
			return testData.equals(decrypted);
		} catch (Exception e) {
			log.error("[SM2密钥] 密钥对验证失败", e);
			return false;
		}
	}
}

