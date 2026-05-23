package com.su60.quickboot.common.encryption.sm2;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.SM2;

/**
 * SM2密钥生成工具
 * 用于生成Base64格式的SM2密钥对
 *
 * @author luyanan
 * @since 2026/02/01
 */
public class SM2KeyGenerator {

	public static void main(String[] args) {
		System.out.println("========================================");
		System.out.println("       SM2密钥对生成工具");
		System.out.println("========================================");
		System.out.println();
		
		// 生成密钥对
		SM2 sm2 = SmUtil.sm2();

		String privateKeyBase64 = sm2.getPrivateKeyBase64();
		String publicKeyBase64 = sm2.getPublicKeyBase64();
		
		System.out.println("✅ 密钥对生成成功！");
		System.out.println();
		
		System.out.println("【私钥】（Base64格式，用于后端配置）");
		System.out.println(privateKeyBase64);
		System.out.println();
		
		System.out.println("【公钥】（Base64格式，用于后端配置）");
		System.out.println(publicKeyBase64);
		System.out.println();
		
		System.out.println("========================================");
		System.out.println("配置示例：");
		System.out.println("========================================");
		System.out.println();
		System.out.println("# application.yml");
		System.out.println("security:");
		System.out.println("  crypto:");
		System.out.println("    enabled: true");
		System.out.println("    public-key: " + publicKeyBase64);
		System.out.println("    private-key: " + privateKeyBase64);
		System.out.println();
		
		System.out.println("========================================");
		System.out.println("密钥信息：");
		System.out.println("========================================");
		System.out.println("私钥长度: " + privateKeyBase64.length() + " 字符");
		System.out.println("公钥长度: " + publicKeyBase64.length() + " 字符");
		System.out.println("密钥格式: Base64编码");
		System.out.println();
		
		// 验证密钥对
		System.out.println("========================================");
		System.out.println("验证密钥对：");
		System.out.println("========================================");
		try {
			String testData = "Hello, SM2 Crypto!";
			
			// 加密
			byte[] encrypted = sm2.encrypt(testData.getBytes(), cn.hutool.crypto.asymmetric.KeyType.PublicKey);
			System.out.println("✅ 加密测试通过");
			
			// 解密
			byte[] decrypted = sm2.decrypt(encrypted, cn.hutool.crypto.asymmetric.KeyType.PrivateKey);
			String decryptedText = new String(decrypted);
			
			if (testData.equals(decryptedText)) {
				System.out.println("✅ 解密测试通过");
				System.out.println("✅ 密钥对验证成功！");
			} else {
				System.out.println("❌ 解密测试失败");
			}
		} catch (Exception e) {
			System.out.println("❌ 密钥对验证失败: " + e.getMessage());
		}
		System.out.println();
		
		System.out.println("========================================");
		System.out.println("⚠️  重要提示：");
		System.out.println("========================================");
		System.out.println("1. 请妥善保管私钥，不要泄露");
		System.out.println("2. 将密钥配置到环境变量或配置文件");
		System.out.println("3. 不要将密钥提交到代码仓库");
		System.out.println("4. 生产环境请使用不同的密钥");
		System.out.println("5. 定期更换密钥");
		System.out.println("========================================");
	}
}

