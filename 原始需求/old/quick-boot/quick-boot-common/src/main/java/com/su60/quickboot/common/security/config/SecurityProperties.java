package com.su60.quickboot.common.security.config;

import com.su60.quickboot.common.sensitive.SensitiveWordStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全相关配置项，支持敏感词、XSS 与 SQL 注入防护。
 */
@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

	private SensitiveWordProperties sensitiveWord = new SensitiveWordProperties();
	private XssProperties xss = new XssProperties();
	private SqlInjectProperties sqlInject = new SqlInjectProperties();

	private Sign sign = new Sign();
	private Crypto crypto = new Crypto();

	@Data
	public static class SensitiveWordProperties {
		private Boolean enabled = false;
		private SensitiveWordStrategy strategy = SensitiveWordStrategy.REPLACE;
		private List<String> ignoreUrls = new ArrayList<>();
		/**
		 * 敏感词白名单文件路径（classpath/file URL），按行读取
		 */
		private String whiteListPath = "classpath:sensitive/white-list.txt";
		/**
		 * 敏感词黑名单文件路径（classpath/file URL），按行读取
		 */
		private String blackListPath = "classpath:sensitive/black-list.txt";
		private Boolean logEnabled = true;
	}

	@Data
	public static class XssProperties {
		private Boolean enabled = false;
		private List<String> ignoreUrls = new ArrayList<>();
	}

	@Data
	public static class SqlInjectProperties {
		private Boolean enabled = false;
		private List<String> ignoreUrls = new ArrayList<>();
		/**
		 * SQL 关键词文件路径（classpath/file URL），按行读取
		 */
		private String keywordsPath = "classpath:sensitive/sql-keywords.txt";
	}


	@Data
	public static class Sign {
		/**
		 * 是否启用签名验证
		 */
		private Boolean enabled = false;

		/**
		 * 签名算法：SM3（国密）
		 */
		private String algorithm = "SM3";

		/**
		 * 签名密钥
		 */
		private String secretKey;

		/**
		 * 签名有效期（秒），默认300秒
		 */
		private Integer expireTime = 300;

		/**
		 * 忽略的url
		 *
		 * @since 2026/1/19
		 */
		private List<String> ignoreUrls = new ArrayList<>();

		/**
		 * IP白名单（白名单内的IP不参与验签）
		 *
		 * @since 2026/1/31
		 */
		private List<String> ipWhitelist = new ArrayList<>();
	}

	@Data
	public static class Crypto {


		private Boolean enabled = false;

		/**
		 * 请求
		 *
		 * @since 2026/2/4
		 */

		private Encryption request = new Encryption();


		/**
		 * 响应
		 *
		 * @since 2026/2/4
		 */


		private Encryption response = new Encryption();
	}


	@Data
	public static class Encryption {

		/**
		 * 是否开启
		 *
		 * @since 2026/2/4
		 */

		private Boolean enable = true;


//		/**
//		 * 公钥
//		 *
//		 * @since 2026/2/4
//		 */
//		private String publicKey;
//
//
//		/**
//		 * 私钥
//		 *
//		 * @since 2026/2/4
//		 */
//		private String privateKey;


		/**
		 * 白名单
		 *
		 * @since 2026/2/4
		 */


		private List<String> ipWhitelist;
	}
}
