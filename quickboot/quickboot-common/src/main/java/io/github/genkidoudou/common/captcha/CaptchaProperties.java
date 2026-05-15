package io.github.genkidoudou.common.captcha;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 业务侧验证码开关与类型；其余项见 Spring Boot 下 {@code captcha.*}（tianai 官方前缀）。
 *
 * @author genkidoudou
 * @see <a href="https://doc.captcha.tianai.cloud/">tianai-captcha 文档</a>
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "qc.captcha")
public class CaptchaProperties {

    /**
     * 是否启用天爱验证码（关闭时不注册本地缓存替换等 Bean）。
     */
    private Boolean enabled = true;

    /**
     * 验证码类型：滑块 SLIDER、旋转 ROTATE、拼接 CONCAT、文字点选 WORD_IMAGE_CLICK 等。
     */
    private String type = "SLIDER";

    /**
     * 缓存实现：{@code local} 使用内存（适合无 Redis 的本地开发）；{@code redis} 使用 {@link org.springframework.data.redis.core.StringRedisTemplate}。
     */
    private String store = "local";
}
