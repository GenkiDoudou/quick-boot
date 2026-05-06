package io.github.genkidoudou.common.security.firewall.password;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link PasswordCodec} 自动配置：在未自定义同类型 Bean 时注册 {@link DefaultPasswordCodec}，
 * 并从 {@code qc.security.firewall.password.codec} 绑定密钥属性后调用 {@link PasswordCodec#setProperties(java.util.Properties)}。
 */
@AutoConfiguration
@EnableConfigurationProperties(FirewallPasswordCodecProperties.class)
public class FirewallPasswordCodecAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PasswordCodec.class)
    public PasswordCodec passwordCodec(FirewallPasswordCodecProperties properties) {
        DefaultPasswordCodec codec = new DefaultPasswordCodec();
        codec.setProperties(properties.toCodecProperties());
        return codec;
    }
}
