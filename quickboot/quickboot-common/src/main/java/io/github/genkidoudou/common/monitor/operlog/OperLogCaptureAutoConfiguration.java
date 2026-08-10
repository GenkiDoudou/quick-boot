package io.github.genkidoudou.common.monitor.operlog;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

/**
 * 注册操作日志采集切面。
 */
@AutoConfiguration
@EnableConfigurationProperties(OperLogProperties.class)
public class OperLogCaptureAutoConfiguration {

  /**
   * 宽切面采集 Bean。
   *
   * @param eventPublisher    事件发布器
   * @param objectMapper      Jackson ObjectMapper
   * @param operLogProperties 采集配置
   * @return 切面实例
   */
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
   *
   * @param objectMapper Jackson ObjectMapper
   * @return 控制台打印监听器
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "qc.monitor.operlog", name = "print", havingValue = "true")
  public OperLogConsolePrintListener operLogConsolePrintListener(ObjectMapper objectMapper) {
    return new OperLogConsolePrintListener(objectMapper);
  }
}
