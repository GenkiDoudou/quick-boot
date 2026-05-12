package com.su60.quickboot.common.security.sign;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * 国密SM3签名工具类
 *
 * @author luyanan
 * @since 2026/01/31
 */
@Slf4j
public class SM3SignatureUtils {

	/**
	 * 生成SM3签名
	 *
	 * @param params    参数Map（会自动按key排序）
	 * @param secretKey 密钥
	 * @return 签名字符串（小写十六进制）
	 */
	public static String generateSignature(Map<String, String> params, String secretKey) {

		if (StrUtil.isBlank(secretKey)) {
			throw new IllegalArgumentException("密钥不能为空");
		}

		// 1. 按key排序
		TreeMap<String, String> sortedParams = new TreeMap<>(params);

		// 2. 拼接参数：key1=value1&key2=value2&key3=value3
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			// 跳过签名字段本身
			if ("sign".equals(key) || "signature".equals(key)) {
				continue;
			}

			// 跳过空值
			if (StrUtil.isBlank(value)) {
				continue;
			}

			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(key).append("=").append(value);
		}

		// 3. 拼接密钥：参数字符串&key=secretKey
		String signContent = Opt.ofBlankAble(sb.toString()).map(a -> a.toString() + "&").orElse("") + "nonce=" + secretKey;


		// 4. 使用Hutool的SM3算法生成签名（返回小写十六进制）
		String signature = SmUtil.sm3(signContent);


		return signature;
	}

	/**
	 * 验证SM3签名
	 *
	 * @param params    参数Map（包含签名字段）
	 * @param secretKey 密钥
	 * @return 是否验证通过
	 */
	public static boolean verifySignature(Map<String, String> params, String secretKey, String sign) {


		// 获取客户端传来的签名
		String clientSign = sign;
//		if (StrUtil.isBlank(clientSign)) {
//			clientSign = params.get("signature");
//		}

//		if (StrUtil.isBlank(clientSign)) {
//			log.warn("[SM3签名] 未找到签名字段，验证失败");
//			return false;
//		}

		// 生成服务端签名
		String serverSign = generateSignature(params, secretKey);


		// 比较签名（忽略大小写）
		boolean valid = serverSign.equalsIgnoreCase(clientSign);


		return valid;
	}

	/**
	 * 验证时间戳是否在有效期内
	 *
	 * @param timestamp  时间戳（秒）
	 * @param expireTime 有效期（秒）
	 * @return 是否有效
	 */
	public static boolean isTimestampValid(Long timestamp, Integer expireTime) {
		if (timestamp == null) {
			return false;
		}

		long currentTime = System.currentTimeMillis() / 1000;
		long diff = Math.abs(currentTime - timestamp);

		boolean valid = diff <= expireTime;
		return valid;
	}
}

