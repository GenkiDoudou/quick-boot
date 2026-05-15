package io.github.genkidoudou.common.logger;

import org.springframework.context.ApplicationEvent;

/**
 * 日志事件
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
public class LoggerEvent extends ApplicationEvent {

    /**
     * 构造日志事件
     *
     * @param source 事件源（LoggerEventDto）
     * @since 2026/03/05
     */
    public LoggerEvent(Object source) {
        super(source);
    }
}
