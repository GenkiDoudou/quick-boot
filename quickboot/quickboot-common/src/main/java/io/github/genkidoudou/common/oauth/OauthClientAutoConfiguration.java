package io.github.genkidoudou.common.oauth;

import io.github.genkidoudou.common.common.Constants;
import io.github.genkidoudou.common.crypto.PasswordCodec;
import io.github.genkidoudou.common.crypto.PasswordCodecFactories;
import io.github.genkidoudou.common.oauth.config.OauthClientProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth 客户端自动配置：注册 {@link ClientBasicAuthenticationFilter} 与 {@code clientBasic} 编解码器。
 */
@Configuration
@EnableConfigurationProperties(OauthClientProperties.class)
@ConditionalOnProperty(prefix = Constants.PROPERTIES_PREFIX + ".oauth", havingValue = "true", matchIfMissing = true, name = "enable")
public class OauthClientAutoConfiguration {

  /**
   * @param oauthServiceSupport  客户端查询 SPI
   * @param oauthClientProperties 模块配置
   * @return Client Basic 认证过滤器
   */
  @Bean
  public ClientBasicAuthenticationFilter clientBasicAuthenticationFilter(OauthServiceSupport oauthServiceSupport, OauthClientProperties oauthClientProperties) {
    PasswordCodecFactories.register("clientBasic", new ClientBasicPasswordCodes(oauthClientProperties.getKey()));
    return new ClientBasicAuthenticationFilter(oauthServiceSupport, oauthClientProperties);

  }
}
