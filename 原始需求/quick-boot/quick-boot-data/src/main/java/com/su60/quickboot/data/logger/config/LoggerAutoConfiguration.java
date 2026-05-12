package com.su60.quickboot.data.logger.config;

import com.su60.quickboot.data.logger.LoggingAspect;
import com.su60.quickboot.data.logger.listener.WebPrintLoggerEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 日志自动化配置
 *
 * @author luyanan
 * @since 2023/05/22
 **/
@EnableConfigurationProperties(LoggerProperties.class)
@Configuration
@ConditionalOnProperty(prefix = LoggerProperties.PREFIX, value = "enable", havingValue = "true", matchIfMissing = false)
@Import({WebPrintLoggerEventListener.class})
public class LoggerAutoConfiguration {


	/**
	 * 日志aop拦截
	 *
	 * @return com.github.rainsoil.fastapi.common.core.logger.core.LoggingAspect
	 * @since 2023/05/22
	 */
	@Bean
	public LoggingAspect loggingAspect() {
		return new LoggingAspect();
	}


}
