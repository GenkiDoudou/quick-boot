package io.github.genkidoudou.common.security.firewall.password;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FirewallPasswordCodecAutoConfiguration} 在未自定义 Bean 时注册 {@link PasswordCodec}。
 */
class FirewallPasswordCodecAutoConfigurationTest {

    private static final String KEY_HEX = "0123456789abcdef0123456789abcdef";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FirewallPasswordCodecAutoConfiguration.class));

    @Test
    void registersPasswordCodec_fromProperties() {
        runner.withPropertyValues(
                        "qc.security.firewall.password.codec.sm4.default-key-id=k1",
                        "qc.security.firewall.password.codec.sm4.keys.k1=" + KEY_HEX)
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(PasswordCodec.class);
                    PasswordCodec c = ctx.getBean(PasswordCodec.class);
                    String enc = c.encrypt("ping", "sm4:k1");
                    assertThat(c.matches("ping", enc)).isTrue();
                });
    }
}
