package com.su60.quickboot.data.logger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志配置类
 *
 * @author luyanan
 * @since 2023/05/21
 **/
@ConfigurationProperties(LoggerProperties.PREFIX)
@Data
public class LoggerProperties {

	public static final String PREFIX = "spring.log";

	/**
	 * 是否开启
	 *
	 * @author luyanan
	 * @since 2023/05/22
	 */
	private Boolean enable;


	/**
	 * 是否打印
	 *
	 * @author luyanan
	 * @since 2023/05/22
	 */
	private Boolean print;

	/**
	 * 是否开启链路
	 *
	 * @author luyanan
	 * @since 2023/05/22
	 */
	private Boolean trace;
}
