package io.github.genkidoudou.common.crypto;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PasswordCodecAutoConfiguration {


  @Bean
  public PasswordCodec passwordCodec() {
    return PasswordCodecFactories.createPasswordCodec();
  }
}
