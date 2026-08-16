package io.github.genkidoudou.common.tracing;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 注册 {@link ClientOperationFilter}，在 CORS 之后、业务 Filter 之前写入 clientOperationId MDC。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ClientOperationTracingAutoConfiguration {

  /** 略晚于 CORS（{@code HIGHEST_PRECEDENCE}），尽早供 oper_log / 慢 SQL 读取。 */
  private static final int FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE + 5;

  /**
   * 注册客户端操作追踪 Filter，顺序略晚于 CORS。
   *
   * @return Filter 注册 Bean
   */
  @Bean
  public FilterRegistrationBean<ClientOperationFilter> clientOperationFilterRegistration() {
    FilterRegistrationBean<ClientOperationFilter> reg = new FilterRegistrationBean<>(new ClientOperationFilter());
    reg.setOrder(FILTER_ORDER);
    reg.addUrlPatterns("/*");
    return reg;
  }
}
