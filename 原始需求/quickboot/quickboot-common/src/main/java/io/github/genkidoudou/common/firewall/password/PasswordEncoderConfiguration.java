package io.github.genkidoudou.common.firewall.password;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 密码编码器自动配置
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Slf4j
@Configuration
public class PasswordEncoderConfiguration {

    /**
     * 注册委托密码编码器
     *
     * @return 委托密码编码器
     * @since 2026/03/05
     */
    @Bean
    @ConditionalOnMissingBean
    public DelegatingPasswordEncoder passwordEncoder() {
        log.info("初始化委托密码编码器，默认使用 bcrypt 算法");
        return new DelegatingPasswordEncoder();
    }
}
