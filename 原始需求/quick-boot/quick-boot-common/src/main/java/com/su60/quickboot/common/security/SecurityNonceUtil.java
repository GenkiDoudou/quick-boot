package com.su60.quickboot.common.security;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;

/**
 * 后端 Nonce 工具类 (与前端逻辑完全对称)
 */
public class SecurityNonceUtil {

	/**
	 * 生成 Nonce
	 */
	public static String generate(String salt) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		StringBuilder obscuredHex = new StringBuilder();

		for (int i = 0; i < timestamp.length(); i++) {
			// 循环异或
			int mixed = timestamp.charAt(i) ^ salt.charAt(i % salt.length());
			obscuredHex.append(String.format("%02x", mixed));
		}

		String prefix = RandomUtil.randomString(4);
		String suffix = RandomUtil.randomString(4);
		return Base64.encode(prefix + obscuredHex.toString() + suffix);
	}

	/**
	 * 从 Nonce 解析 Timestamp
	 */
	public static long parse(String nonce, String salt) {
		try {
			String decoded = Base64.decodeStr(nonce);
			// 截取核心密文
			String hex = decoded.substring(4, decoded.length() - 4);
			StringBuilder timestampStr = new StringBuilder();

			for (int i = 0; i < hex.length() / 2; i++) {
				String hexPair = hex.substring(i * 2, i * 2 + 2);
				int mixed = Integer.parseInt(hexPair, 16);
				// 再次异或还原
				char original = (char) (mixed ^ salt.charAt(i % salt.length()));
				timestampStr.append(original);
			}
			return Long.parseLong(timestampStr.toString());
		} catch (Exception e) {
			return -1L;
		}
	}

	public static void main(String[] args) {
		String sat = "6ee81d6c6226611c5b302dae908f8d2bf0b21cda8cadbca34a253831388c8d1e";
		String nonce = generate(sat);
		System.out.println(nonce);
		System.out.println(parse(nonce, sat));
	}
}