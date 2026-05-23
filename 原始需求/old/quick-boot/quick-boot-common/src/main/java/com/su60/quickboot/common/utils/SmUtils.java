package com.su60.quickboot.common.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.SM4;
import lombok.Data;
import lombok.experimental.UtilityClass;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * 国密算法工具类（SM2 + SM4 CBC）
 * 依赖：Hutool-all、bcprov-jdk18on
 * 用途：后端解密、验签、SM2密钥对生成核心方法封装
 */
@UtilityClass
public class SmUtils {


	/**
	 * 加密
	 *
	 * @return 明文
	 */
	public static String encryptSM4(String plaintext, String sm4KeyHex, String ivHex) {
		// 1. 验证参数（生产环境建议保留）
		if (sm4KeyHex == null || sm4KeyHex.length() != 32 || !sm4KeyHex.matches("[0-9a-f]{32}")) {
			throw new IllegalArgumentException("密钥必须是32位小写十六进制字符串");
		}
		if (ivHex == null || ivHex.length() != 32 || !ivHex.matches("[0-9a-f]{32}")) {
			throw new IllegalArgumentException("IV必须是32位小写十六进制字符串");
		}

		// 2. 转换密钥和IV为字节数组
		byte[] keyBytes = HexUtil.decodeHex(sm4KeyHex.toLowerCase());
		byte[] ivBytes = HexUtil.decodeHex(ivHex.toLowerCase());

		// 3. 初始化SM4（CBC + PKCS5Padding = 前端PKCS7Padding）
		SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, keyBytes, ivBytes);


		return sm4.encryptHex(plaintext);
	}

	/**
	 * 解密
	 *
	 * @param plaintext 密文
	 * @return 铭文
	 */
	public static String decryptSm4(String plaintext, String sm4KeyHex, String ivHex) {
		// 2. 转换密钥和IV为字节数组
		byte[] keyBytes = HexUtil.decodeHex(sm4KeyHex.toLowerCase());
		byte[] ivBytes = HexUtil.decodeHex(ivHex.toLowerCase());
		SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, keyBytes, ivBytes);
		return sm4.decryptStr(plaintext);

	}

	public String genKey() {
		return RandomUtil.randomString("123456789abcdef", 32);
	}

	/**************SM2****************/

	/**
			* 生成与前端完全兼容的密钥对（十六进制格式）
			*/
	public static KeyPair generateCompatibleKeyPair() throws Exception {
		SM2 sm2 = new SM2();
		KeyPair pair = new KeyPair();

		// 转换公钥：ASN.1 -> 未压缩点十六进制 (130字符)
		byte[] pubEncoded = sm2.getPublicKey().getEncoded();
		SubjectPublicKeyInfo pubInfo = SubjectPublicKeyInfo.getInstance(pubEncoded);
		byte[] pubPoint = pubInfo.getPublicKeyData().getBytes(); // 直接获取未压缩点
		// 验证格式（SM2 公钥应为 65 字节未压缩点）
		if (pubPoint.length != 65 || pubPoint[0] != 0x04) {
			// 备用方案：通过 BouncyCastle 重新编码
			ECPublicKeyParameters pubParams = (ECPublicKeyParameters)
					PublicKeyFactory.createKey(pubEncoded);
			pubPoint = pubParams.getQ().getEncoded(false); // false=未压缩
		}
		pair.setPublicKey(HexUtil.encodeHexStr(pubPoint)); // 130字符

		// 转换私钥：ASN.1 -> 纯私钥值十六进制 (64字符)
		byte[] priEncoded = sm2.getPrivateKey().getEncoded();
		ECPrivateKeyParameters priParams = (ECPrivateKeyParameters)
				PrivateKeyFactory.createKey(priEncoded);
		BigInteger d = priParams.getD();

		// 确保 32 字节（64 字符十六进制）
		byte[] dBytes = d.toByteArray();
		if (dBytes.length > 32) {
			dBytes = Arrays.copyOfRange(dBytes, dBytes.length - 32, dBytes.length);
		} else if (dBytes.length < 32) {
			byte[] temp = new byte[32];
			System.arraycopy(dBytes, 0, temp, 32 - dBytes.length, dBytes.length);
			dBytes = temp;
		}
		pair.setPrivateKey(HexUtil.encodeHexStr(dBytes)); // 64字符

		return pair;
	}

	// 加密（接收十六进制公钥）
	public static String encryptSM2(String plaintext, String publicKeyHex) {
		// Hutool 自动识别十六进制字符串
		SM2 sm2 = new SM2(null, publicKeyHex);
		sm2.setMode(SM2Engine.Mode.C1C3C2);
		return sm2.encryptHex(plaintext, KeyType.PublicKey);
	}

	// 解密（接收十六进制私钥）
	public static String decryptSM2(String cipherBase64, String privateKeyHex) {
		SM2 sm2 = new SM2(privateKeyHex, null); // Hutool 自动识别十六进制
		sm2.setMode(SM2Engine.Mode.C1C3C2);
		return sm2.decryptStr(cipherBase64, KeyType.PrivateKey);
	}

	public static void main(String[] args) throws Exception {
//		System.out.println("========================================");
//		String s = genKey();
//		System.out.println(s);
//		String s1 = SmUtils.encryptSM4("1234567", "8c93267dca34a336eeed6b2281f1551a", "8c93267dca34a336eeed6b2281f1551a");
//		System.out.println("加密之后:" + s1);
//		String s2 = SmUtils.decryptSm4(s1, "8c93267dca34a336eeed6b2281f1551a", "8c93267dca34a336eeed6b2281f1551a");
//		System.out.println("解密之后:" + s2);

		String text = "我是一段测试aaaa";

		KeyPair keyPair = generateCompatibleKeyPair();
		String publicKeyHex = keyPair.getPublicKey();
		String privateKeyHex = keyPair.getPrivateKey();

//
//		String privateKeyHex ="1fc18ec7308783e5a0001961d1b278f051d069e0f1bbcbac1be9fe5e51eee8a0";
//		String publicKeyHex = "041fa583a843a82296e416938f815629b4591c39fa96dc1de0b5a40461739f03aca58664037a60a6ca3ae2576a59eb788c75192a13bab768fa9c6425bf654197de";
		System.out.println("私钥：" + privateKeyHex);
		System.out.println("公钥：" + publicKeyHex);
//		String encryptStr = encryptSM2(text, publicKeyHex);
//		System.out.println("公钥加密：" + encryptStr);
////		String decryptStr = decryptSM2(encryptStr, privateKeyHex);
//		String decryptStr = decryptSM2(encryptStr, privateKeyHex);
////		String decryptStr = decryptSM2("04fa4fad37667b43b6d95bfcc96e77acfe8e743e4ca4c422bec48d61ace0dce46c8a4dbdc25ce53c6c62e8cd1eba1017503bc13a7d908cd0b4e6f4e25a4858e2ca7aa4854c68001775deae9213188fbece9102dd6f4397c996cdd502d04045e1113897dbf05fe2b8db38713fb21afa7649ee35437f57f133c1a105aeca361a5bb77bd81549f14bd15322e1346bdb32d86f6c5aff3e293ad56782d9b031424cd94cc5",
//
////				"1fc18ec7308783e5a0001961d1b278f051d069e0f1bbcbac1be9fe5e51eee8a0");
//		System.out.println("公钥加密，私钥解密：" + decryptStr);
//		System.out.println("========================================");
	}

	/**
	 * 密钥对（Base64格式）
	 */
	@Data
	public static class KeyPair {
		private String publicKey;  // Base64
		private String privateKey; // Base64
	}


}