package io.github.genkidoudou.common.monitor.operlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * 注册操作日志采集切面。
 */
@AutoConfiguration
@EnableConfigurationProperties(OperLogProperties.class)
public class OperLogCaptureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "qc.monitor.operlog", name = "capture-enabled", havingValue = "true", matchIfMissing = true)
    public OperLogPublishingAspect operLogPublishingAspect(
        ApplicationEventPublisher eventPublisher,
        ObjectMapper objectMapper,
        OperLogProperties operLogProperties
    ) {
        return new OperLogPublishingAspect(eventPublisher, objectMapper, operLogProperties);
    }

    /**
     * 控制台打印 Web 请求日志（需 {@code qc.monitor.operlog.print=true}）。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "qc.monitor.operlog", name = "print", havingValue = "true")
    public OperLogConsolePrintListener operLogConsolePrintListener(ObjectMapper objectMapper) {
        return new OperLogConsolePrintListener(objectMapper);
    }
}
