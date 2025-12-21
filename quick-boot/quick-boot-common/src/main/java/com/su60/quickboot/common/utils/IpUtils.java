package com.su60.quickboot.common.utils;

import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.Ip2Region;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

/**
 * IP 工具类（ip2region + 内网判断）
 *
 * 特点：
 * 1. 不依赖 Spring Bean（但使用 Spring 的 Resource 工具）
 * 2. JVM 级别单例
 * 3. 支持 IPv4 / IPv6
 * 4. 内网判断采用 RuoYi 标准实现
 *
 * xdb 文件位置：
 * classpath:/ip2region/ip2region.xdb
 * classpath:/ip2region/ip2region_v6.xdb
 */
public final class IpUtils {

	/** 内网 IP 返回值 */
	private static final String INNER_IP_REGION = "内网 IP";

	/** ip2region 查询实例（全局唯一） */
	private static final Ip2Region IP2_REGION;

	static {
		try {
			// ======================= 加载 IPv4 xdb =======================
			ClassPathResource v4Resource =
					new ClassPathResource("ip2region/ip2region_v4.xdb");
			InputStream v4InputStream = v4Resource.getInputStream();

			Config v4Config = Config.custom()
					.setCachePolicy(Config.BufferCache)   // 使用 InputStream 必须是 BufferCache
					.setSearchers(8)
					.setXdbInputStream(v4InputStream)
					.asV4();

			// ======================= 加载 IPv6 xdb =======================
			ClassPathResource v6Resource =
					new ClassPathResource("ip2region/ip2region_v6.xdb");
			InputStream v6InputStream = v6Resource.getInputStream();

			Config v6Config = Config.custom()
					.setCachePolicy(Config.BufferCache)
					.setSearchers(4)
					.setXdbInputStream(v6InputStream)
					.asV6();

			// ======================= 创建查询服务 =======================
			IP2_REGION = Ip2Region.create(v4Config, v6Config);

		} catch (Exception e) {
			throw new ExceptionInInitializerError(
					"IpUtils 初始化 ip2region 失败: " + e.getMessage()
			);
		}
	}

	/** 禁止实例化 */
	private IpUtils() {}

	// ============================ 对外 API ============================

	/**
	 * 根据 IP 获取地理位置
	 *
	 * @param ip IPv4 / IPv6
	 * @return 内网 IP | 国家|省份|城市|运营商
	 */
	public static String getRegion(String ip) {
		if (ip == null || ip.isEmpty()) {
			return INNER_IP_REGION;
		}

		// IPv4 私网判断（RuoYi 方案）
		if (isInnerIp(ip)) {
			return INNER_IP_REGION;
		}

		try {
			return IP2_REGION.search(ip);
		} catch (Exception e) {
			return "未知";
		}
	}

	// ============================ 内网判断（RuoYi 原版） ============================

	/**
	 * 判断是否内网 IP
	 */
	public static boolean isInnerIp(String ip) {
		byte[] addr = textToNumericFormatV4(ip);
		return internalIp(addr) || "127.0.0.1".equals(ip);
	}

	private static boolean internalIp(byte[] addr) {
		if (addr == null || addr.length < 2) {
			return true;
		}

		final byte b0 = addr[0];
		final byte b1 = addr[1];

		// 10.x.x.x
		if (b0 == 0x0A) {
			return true;
		}

		// 172.16.x.x ~ 172.31.x.x
		if (b0 == (byte) 0xAC && b1 >= (byte) 0x10 && b1 <= (byte) 0x1F) {
			return true;
		}

		// 192.168.x.x
		return b0 == (byte) 0xC0 && b1 == (byte) 0xA8;
	}

	/**
	 * IPv4 字符串转 byte[]
	 */
	public static byte[] textToNumericFormatV4(String text) {
		if (text == null || text.isEmpty()) {
			return null;
		}

		String[] elements = text.split("\\.");
		if (elements.length != 4) {
			return null;
		}

		byte[] bytes = new byte[4];
		try {
			for (int i = 0; i < 4; i++) {
				int val = Integer.parseInt(elements[i]);
				if (val < 0 || val > 255) {
					return null;
				}
				bytes[i] = (byte) val;
			}
		} catch (NumberFormatException e) {
			return null;
		}
		return bytes;
	}

	// ============================ 关闭资源（可选） ============================

	/**
	 * JVM 关闭时可手动调用（一般不需要）
	 */
	public static void close() {
		if (IP2_REGION != null) {
			try {
				IP2_REGION.close();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void main(String[] args) {
			System.out.println(getRegion("127.0.0.1"));
			System.out.println(getRegion("120.244.163.243"));
	}
}
