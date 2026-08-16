package io.github.genkidoudou.common.crypto;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 密码编解码器 Spring 装配：注册默认 {@link DelegatingPasswordCodec}（bcrypt + sm3）。
 */
@Configuration
public class PasswordCodecAutoConfiguration {

  /**
   * @return 应用级 {@link PasswordCodec} Bean，默认 id 为 {@code bcrypt}
   */
  @Bean
  public PasswordCodec passwordCodec() {
    return PasswordCodecFactories.createPasswordCodec();
  }
}
