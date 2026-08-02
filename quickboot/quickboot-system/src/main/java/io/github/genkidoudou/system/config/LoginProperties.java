package io.github.genkidoudou.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 登录失败锁定等业务配置。
 */
@Data
@ConfigurationProperties(prefix = "qc.login")
public class LoginProperties {

  /**
   * 连续失败次数上限，达到后写入锁定键。
   */
  private int maxRetry = 5;

  /**
   * 锁定分钟数（锁定键 TTL）。
   */
  private int lockMinutes = 10;
}
