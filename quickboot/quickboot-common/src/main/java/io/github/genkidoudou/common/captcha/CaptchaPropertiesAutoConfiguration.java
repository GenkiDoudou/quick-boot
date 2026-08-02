package io.github.genkidoudou.common.captcha;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 无论验证码开关如何，都绑定 {@link CaptchaProperties}，并暴露 {@code /api/captcha/config}。
 */
@AutoConfiguration
@EnableConfigurationProperties(CaptchaProperties.class)
@Import(CaptchaConfigController.class)
public class CaptchaPropertiesAutoConfiguration {
}
