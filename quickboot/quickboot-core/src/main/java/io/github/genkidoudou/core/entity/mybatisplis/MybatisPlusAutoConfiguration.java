package io.github.genkidoudou.core.entity.mybatisplis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 扩展配置：注册审计字段自动填充处理器。
 */
@Configuration
public class MybatisPlusAutoConfiguration {


  /**
   * 注册 {@link MyMetaObjectHandler}，在 INSERT/UPDATE 时填充 {@link io.github.genkidoudou.core.entity.BaseEntity} 审计列。
   *
   * @return MetaObjectHandler Bean
   */
  @Bean
  public MyMetaObjectHandler myMetaObjectHandler() {
    return new MyMetaObjectHandler();
  }
}
