package io.github.genkidoudou.web.config;

import ch.vorburger.mariadb4j.DB;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * 将 {@link DevMariaDb4jStartupListener} 启动的 DB 注册为 Spring Bean，
 * 便于容器销毁时 stop（shutdown hook 仍作兜底）。
 */
@AutoConfiguration
@Profile("dev")
@ConditionalOnProperty(prefix = "qc.dev.embedded-mariadb", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DevMariaDb4jConfiguration {

  @Bean(destroyMethod = "stop")
  public DB embeddedMariaDb() {
    DB db = DevMariaDb4jStartupListener.embeddedDb();
    if (db == null) {
      throw new IllegalStateException(
        "Embedded MariaDB was not started; check DevMariaDb4jStartupListener / qc.dev.embedded-mariadb.*"
      );
    }
    return db;
  }
}
