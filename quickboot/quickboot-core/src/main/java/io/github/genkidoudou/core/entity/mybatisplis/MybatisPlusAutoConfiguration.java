package io.github.genkidoudou.core.entity.mybatisplis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusAutoConfiguration {


  @Bean
  public MyMetaObjectHandler myMetaObjectHandler() {
    return new MyMetaObjectHandler();
  }
}
