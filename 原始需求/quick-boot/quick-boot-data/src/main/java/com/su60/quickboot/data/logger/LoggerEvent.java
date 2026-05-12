package com.su60.quickboot.data.logger;

import org.springframework.context.ApplicationEvent;

/**
 * 日志事件
 *
 * @author luyanan
 * @since 2024/04/27
 **/
public class LoggerEvent extends ApplicationEvent {
	public LoggerEvent(Object source) {
		super(source);
	}
}
