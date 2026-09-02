package io.github.genkidoudou.web.support;

import io.github.genkidoudou.web.config.DevMariaDb4jStartupListener;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 集成测试启动嵌入式 MariaDB：{@link DevMariaDb4jStartupListener} 仅在 {@code WebApplication#main} 注册，
 * {@code @SpringBootTest} 须显式调用 {@link DevMariaDb4jStartupListener#startIfNeeded}。
 */
public class QuickbootDevInfrastructureInitializer
  implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    ConfigurableEnvironment environment = applicationContext.getEnvironment();
    DevMariaDb4jStartupListener.startIfNeeded(environment);
  }
}
