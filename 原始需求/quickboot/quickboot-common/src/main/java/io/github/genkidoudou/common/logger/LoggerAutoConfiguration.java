package io.github.genkidoudou.common.logger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 日志记录自动配置
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LoggerProperties.class)
@ConditionalOnProperty(prefix = "qc.logger", name = "enabled", havingValue = "true", matchIfMissing = false)
public class LoggerAutoConfiguration {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 注册日志拦截切面
     *
     * @return 日志拦截切面
     * @since 2026/03/05
     */
    @Bean
    public LoggingAspect loggingAspect() {
        log.info("初始化日志拦截切面");
        return new LoggingAspect(eventPublisher);
    }
}
