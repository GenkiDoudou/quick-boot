package com.su60.quickboot.common.security;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * Jasypt工具类
 *
 * @author luyanan
 * @since 2026/1/18
 */
public class JasyptUtils {
	public static void main(String[] args) {
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPassword("qc2026");
		config.setAlgorithm("PBEWithHMACSHA256AndAES_128");
		config.setKeyObtentionIterations("1000");
		config.setPoolSize("1");
		config.setProviderName("SunJCE");
		config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
		config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
		config.setStringOutputType("base64");
		encryptor.setConfig(config);
		String encryptedText = encryptor.encrypt("rootroot");
		System.out.println(encryptedText);
	}
}
