package io.github.genkidoudou.common.oauth.config;

import io.github.genkidoudou.common.common.Constants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * OAuth 客户端模块配置，前缀 {@code qc.oauth}。
 */
@Data
@ConfigurationProperties(prefix = Constants.PROPERTIES_PREFIX + ".oauth")
public class OauthClientProperties {

  /**
   * 是否启用 Client Basic 过滤器与相关 Bean。
   */
  private boolean enable = false;

  /** Client Basic 凭证 XOR 混淆密钥（见 {@link ClientBasicPasswordCodes}）。 */
  private String key;

  /**
   * 跳过 Client Basic 校验的 URL 模式（Ant 风格）。
   */
  private List<String> ignoreUrl;


}
