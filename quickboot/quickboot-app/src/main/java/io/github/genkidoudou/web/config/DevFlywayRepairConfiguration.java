package io.github.genkidoudou.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 本地开发：migrate 前先 repair，对齐已改动脚本（如 V15）的 checksum。
 * <p>{@code spring.flyway.repair-on-migrate} 并非 Spring Boot 官方项，故用策略显式 repair。</p>
 */
@Configuration
@Profile("dev")
public class DevFlywayRepairConfiguration {

  private static final Logger log = LoggerFactory.getLogger(DevFlywayRepairConfiguration.class);

  @Bean
  public FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
      log.info("Flyway repair (dev) before migrate");
      flyway.repair();
      flyway.migrate();
    };
  }
}
