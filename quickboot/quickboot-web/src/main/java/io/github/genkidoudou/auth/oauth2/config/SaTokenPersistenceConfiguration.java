package io.github.genkidoudou.auth.oauth2.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * {@code qc.oauth2.token-store=local} 时使用内存 {@link SaTokenDao}，无需 Redis。
 */
@Configuration
@ConditionalOnProperty(prefix = "qc.oauth2", name = "token-store", havingValue = "local", matchIfMissing = true)
public class SaTokenPersistenceConfiguration {

    /**
     * 显式注册内存 DAO，避免 classpath 上存在 redis 插件时误用 Redis 实现。
     */
    @Bean
    @Primary
    public SaTokenDao saTokenDao() {
        return new SaTokenDaoDefaultImpl();
    }
}
