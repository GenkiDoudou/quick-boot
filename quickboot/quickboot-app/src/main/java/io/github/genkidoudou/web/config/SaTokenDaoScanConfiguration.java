package io.github.genkidoudou.web.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.dao.SaTokenDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 注册基于 {@code SCAN} 的 Sa-Token Redis DAO，覆盖默认 {@code KEYS} 实现。
 */
@Configuration
public class SaTokenDaoScanConfiguration {

  @Bean
  @Primary
  public SaTokenDao saTokenDao(RedisConnectionFactory connectionFactory) {
    SaTokenDaoForRedisTemplateScan dao = new SaTokenDaoForRedisTemplateScan();
    dao.init(connectionFactory);
    SaManager.setSaTokenDao(dao);
    return dao;
  }
}
